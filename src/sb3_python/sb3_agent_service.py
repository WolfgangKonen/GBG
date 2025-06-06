from agent_trainer import Agent
from environment_connector import GBGEnvironmentClient
import utils
import os
import signal
from uuid import UUID
from typing import Dict, Union, Optional
import numpy as np
from pydantic import BaseModel, Field
from stable_baselines3 import DQN, PPO
from sb3_contrib import MaskablePPO
from fastapi import BackgroundTasks, FastAPI, Response
import logging


# start with: uvicorn sb3_agent_service:app --host 0.0.0.0 --port 8095 --log-level warning
# start TensorBoard with: tensorboard --logdir ./app/logs
# API docs: http://127.0.0.1:8095/docs

app = FastAPI()

agents: dict[UUID, Agent] = dict()


class SelfPlayParameters(BaseModel):
    """
    The Parameters used for self play.
    """
    policyWindowSize: int = Field(description="How many old policy's/ models should be saved.", gt=0)
    addPolicyEveryXSteps: int = Field(description="Add current the policy every X steps. Determines how old policy should be are.",
                                      gt=0)
    useLatestPolicy: float = Field(description="How often should the latest policy be used for self play.", ge=0, le=1)


class EvaluationOptions(BaseModel):
    """
    The Options used for evaluation.
    """
    evaluateEverySteps: int = Field(description="How often should be evaluated.", gt=0)
    numberOfGames: int = Field(description="numberOfGames for each player on each playerPostion (e.g. X or O in TicTacToe). So Total number of games = numberOfGames * players.", gt=0)
    opponent: str = Field(description="Name of opponent used for evaluation.")
    saveBest: bool = Field(description="Save Policy if better")


class EnvironmentParameters(BaseModel):
    """
    The Parameters used to creat a gym.env.
    """
    actionSpaceSize: int = Field(gt=0, description="Size of all actions that can be available.")
    observationRangeStarts: list[int] = Field(description="The starts of the integer values of the observationVector.")
    observationRangeSizes: list[int] = Field(
        description="Size of the ranges the integer values of the observationVector can fall in.")


class SB3Parameters(BaseModel):
    """
    The Parameters used to creat a gym.env and a stable baseline agent.
    """
    agent_id: UUID = Field(
        description="The agents Id used for loading and saving and also impotent when several agents are used.")
    agentType: str = Field(description="Type of sb3 agent that should be created.")  # TODO: enums
    gameName: str = Field(description="Name of the game. e.g: TicTacToe.")

    baseParameters: Dict[str, Union[str, int, float, bool, None]] = Field(
        description="Parameters every sb3 agent has in common.")
    agentParameters: Dict[str, Union[str, int, float, bool, None]] = Field(
        description="Special parameters specific to agentType.")
    networkParameters: Dict[str, Union[str, int, float, bool, list, None]] = Field(
        description="Parameter to creat the neural Network inside the agent.")
    environmentParameters: EnvironmentParameters = Field(description="Parameters needed to creat the gym.Env")


@app.post("/agents")
async def create_agent(sb3_parameters: SB3Parameters):
    """
    Create a new agent environment for the agent.
    :param sb3_parameters: Parameters needed to creat the agent and the env for the agent
    :return:
    """
    global agents
    print(f"Creating new Agent of Type: {sb3_parameters.agentType}")
    print(f"AgentID : {sb3_parameters.agent_id}")
    env = GBGEnvironmentClient(sb3_parameters.environmentParameters)
    agents[sb3_parameters.agent_id] = Agent.form_parameters(sb3_parameters.agent_id,
                                                            env,
                                                            sb3_parameters.agentType,
                                                            sb3_parameters.gameName,
                                                            sb3_parameters.baseParameters,
                                                            sb3_parameters.agentParameters,
                                                            sb3_parameters.networkParameters)
    return Response("Agent created", media_type="text/plain", status_code=201)


class LearningParameters(BaseModel):
    """
    Learning parameters needed to start the training process of an Agent.
    """
    totalTimeSteps: int = Field(gt=0)
    evaluationOptions: EvaluationOptions
    selfPlayParameters: Optional[SelfPlayParameters] = Field(
        "Parameters to use for self play. Only required if you want to use self play.")


@app.post("/agents/{agent_id}/learn")
async def learn(agent_id: UUID, learning_parameters: LearningParameters, background_tasks: BackgroundTasks):
    """
    starts training the Agent.
    """
    global agents

    background_tasks.add_task(agents[agent_id].learn, total_time_steps=learning_parameters.totalTimeSteps, self_play_parameters=learning_parameters.selfPlayParameters, evaluation_options=learning_parameters.evaluationOptions)
    return Response("training started", media_type="text/plain")


class PredictRequest(BaseModel):
    """
    Request used to predict the next action for an Agent.
    """
    observation: list[int] = Field(description="Observation without any preprocessing.")
    availableActions: list[int] = Field(description="Available actions for Action Mask: List of actions (ints) that are available")
    deterministic: bool = Field(default=True, description="If ture returns the Action with the maximum value, if false get a action with the probepility of the value. Diffrent to GBG deteministic in getNextAction2()")


class PredictResponse(BaseModel):
    """
    Response model to a predict request.
    """
    action: int = Field(description="Best action.")
    actionValues: list[float] = Field(description="Values of Actions with not available actions cut out.")


@app.post("/agents/{agent_id}/predict", response_model=PredictResponse)
async def predict(agent_id: UUID, predict_request: PredictRequest) -> PredictResponse:
    """
    Respond the best available action and values of available actions, with not available actions cut out; Order stays the same.
    If deterministic is true returns the action with the highest value else returns an action chosen with the value as a probepility.
    """
    global agents
    agent = agents[agent_id]

    action, action_values = agent.predict(np.array(predict_request.observation), np.array(predict_request.availableActions), predict_request.deterministic)
    response = PredictResponse(action=action, actionValues=action_values)
    return response


@app.post("/agents/{agent_id}/save")
async def save(agent_id: UUID):
    """
    Saves the agents policy at the location specified in config.yaml file under "gbg_save_location: ".
    """
    agents[agent_id].save()


class LoadRequest(BaseModel):
    """
    Request with parameters needed to load an agent.
    """
    agentType: str = Field(description="Name of the agent. e.g: PPO, DQN, MPPO.")
    gameName: str = Field(None, description="Name of the game. e.g: TTT, C4.")
    path: str = Field(None, description="Load a specific model. If None load latest.")


@app.post("/agents/{agent_id}/load")
async def load(agent_id: UUID, load_request: LoadRequest):
    """
    Load a specific model. If no directory path is provided loads latest.
    """
    global agents
    if load_request.path is None:
        agent_path = f"{utils.get_save_dir()}/{load_request.gameName}/SB3Agent/{load_request.agentType}/{str(agent_id)}"
        files = os.listdir(f"{agent_path}/")
        latest = utils.get_latest_file(files)
        path = f"{agent_path}/{latest}"
    else:
        path = load_request.path


    match load_request.agentType:
        case "DQN":
            agents[agent_id] = Agent(agent_id, DQN.load(path), load_request.agentType, load_request.gameName)
        case "PPO":
            agents[agent_id] = Agent(agent_id, PPO.load(path), load_request.agentType, load_request.gameName)
        case "MPPO":
            agents[agent_id] = Agent(agent_id, MaskablePPO.load(path), load_request.agentType, load_request.gameName)

    print(f"Agent Loaded: {path}")


class SelfPlayResponse(BaseModel):
    """
    Response model for a selfPlay Request.
    """
    action: int = Field(description="Chosen action by the agent.")


@app.post("/agents/{agent_id}/selfPlay", response_model=SelfPlayResponse)
async def selfPlay(agent_id: UUID, predict_request: PredictRequest) -> SelfPlayResponse:
    """
    Respond with the best available action form a random selfPlayPolicy.
    If deterministic is true returns the action with the highest value else returns an action chosen with the value as
    a probability.
    """
    global agents
    action, _ = agents[agent_id].predict(np.array(predict_request.observation), np.array(predict_request.availableActions), predict_request.deterministic, True)
    self_play_response = SelfPlayResponse(action=action)
    return self_play_response


# stop app
def exit_app():
    print("Killing process.")
    os.kill(os.getpid(), signal.SIGINT)


# starts the app
if __name__ == '__main__':
    import uvicorn
    print("started")
    uvicorn.run(app, host='0.0.0.0', port=utils.get_port(), log_level=logging.WARNING)
