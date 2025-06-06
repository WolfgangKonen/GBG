from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:  # Only imports the below statements during type checking
    from sb3_agent_service import EnvironmentParameters
import utils


import numpy as np
import gymnasium as gym
import requests
from pydantic import BaseModel


class ResetResponse(BaseModel):
    observationVector: list[float]
    info: dict[str, str]


class StepResponse(BaseModel):
    observationVector: list[float]
    reward: float
    terminated: bool
    truncated: bool
    info: dict[str, str]


class GBGEnvironmentClient(gym.Env):
    """
    Inherent from gym.Env. Connects via HTTP request to the GBG SB3 API, so the games/ environments in the GBG app can be used by SB3 Agents.
    So it does not implement any real logic about the game, it just delegates the reset, step functions to the GBG SB3 API.
    """
    def __init__(self, environment_parameters: EnvironmentParameters):
        self.observation_space = gym.spaces.MultiDiscrete(environment_parameters.observationRangeSizes,
                                                          start=environment_parameters.observationRangeStarts)
        self.action_space = gym.spaces.Discrete(environment_parameters.actionSpaceSize)

        self.session = requests.Session()
        self.session.headers.update({'Connection': 'keep-alive'})

    def reset(self, *, seed: int | None = None, options=None, ):
        """
        Mainly retrieves the first Observation from the GBG SB3 API.
        """
        super().reset(seed=seed)
        response = self.session.post(f"http://{utils.get_gbg_ip()}:{utils.get_gbg_port()}/reset")
        response.raise_for_status()
        reset_response = ResetResponse(**response.json())

        observation_vector = np.array(reset_response.observationVector)
        return observation_vector, reset_response.info

    def step(self, action: int):
        """
        Sends the chosen action by the Agent to the GBG SB3 API and returns the resulting observation, reward and if the game is terminated.
        """
        response = self.session.post(f"http://{utils.get_gbg_ip()}:{utils.get_gbg_port()}/step", json={"action": int(action)})
        response.raise_for_status()
        step_response = StepResponse(**response.json())

        observation_vector = np.array(step_response.observationVector)
        return observation_vector, step_response.reward, step_response.terminated, step_response.truncated, step_response.info

    def action_masks(self) -> np.ndarray[bool]:
        """
        Only used by MaskablePPO. Retrieves the availableActions from the GBG SB3 API and converts them to a mask.
        :return: Action masks with true if valid and false if invalid
        """
        response = self.session.get(f"http://{utils.get_gbg_ip()}:{utils.get_gbg_port()}/availableActions")
        response.raise_for_status()
        action_mask = np.zeros(self.action_space.n, dtype=bool)
        for a in response.json():
            action_mask[a] = True

        return action_mask
