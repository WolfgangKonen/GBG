# SB3-Python-app
This directory contains the SB3-Python-App, which is relevant if you want to train [Stable Baselines3](https://stable-baselines3.readthedocs.io/en/master/) (SB3) agents in the GBG.

SB3 provides deep reinforcement learning agents. This project integrates the following algorithms into the GBG:
- Deep Q-Learning (DQN)
- Proximal Policy Optimization (PPO)
- Maskable Proximal Policy Optimization (MPPO)

The SB3-Python-App provides an API that GBG uses to interface with SB3 and train agents. When training an SB3 agent, GBG also exposes an API to allow bidirectional communication. The GBG-side implementation of this interface can be found [here](./../controllers/SB3).

The server for SB3-Python-App is implemented using [FastApi](https://fastapi.tiangolo.com/).

Below are the steps to set up and use this application.


## Installation
Use Python 3.11 or higher. First, install all necessary requirements. It is recommended to use a [python venv](https://docs.python.org/3/library/venv.html) and installing the requirements there to keep dependencies organized.
After activating the venv use the python pip command to install all requirements of the [requirements.txt](./requirements.txt) with the following command:
```
pip install -r requirements.txt
```

## Start SB3-Python-App
Now you can start the SB3-Python-App either with [uvicorn](https://www.uvicorn.org/), or by directly running the script [sb3_agnet_service.py](./sb3_agent_service.py).
Ensure the venv is activated before starting the app. Use the commands from this directory.

Command for starting the app using uvicorn: 
```
uvicorn sb3_agent_service:app --host 0.0.0.0 --port 8095 --log-level warning
```
Or start python script:
```
python ./sb3_agent_service.py
```

## Useful tips
### TensorBoard
To view training logs with [TensorBoard](https://www.tensorflow.org/tensorboard), run:
```
tensorboard --logdir ./logs
```
Access the TensorBoard UI at: http://localhost:6006/
### OpenAPI Docs
Once the FastAPI server is running, you can view the automatically generated API documentation at:
http://127.0.0.1:8095/docs
### Parameter Tab
When training an SB3 Agent, it is advised to check out the Parameter Tab of SB3. Here configure a lot:
- Set the Time Steps to train the agent (the "Train Games" input field in the main menu is irrelevant when training SB3 agents).
- If you want to log the training via TensorBoard, you have to uncheck the option "Use Standard Parameters by SB3". And declare the log directory you want to use.
- Under Evaluation options, you can configure how often the SB3 agent gets evaluated. The results are logged by TensorBoard.
- Under Evaluation options, check the option "Save model after evaluation if better" to save the best policy. This option can take up a lot of disk space over time. Make sure to delete unused policies periodically. You can find them here: ```agents/<game>/SB3Agent/<AgentType>/<UUID>```. You still have to save the agent through the main menu, e.g., "Save Agent X," so you can access those policies later.
- If you want to load a specific policy after training an agent, use the "Load Policy" button. It shows all saved policies for that specific agent.
- Under "Opponent Options," you can manage which opponents the agent is trained against. You can also balance the ratio the agent plays against opponents — for example, using 3 times Self Play and 1 time Random Agent means the main agent plays every 4th game against Random and otherwise uses Self Play.
- Use the "Network Parameters" button to configure the neural network used in the SB3 agent.
- Set many other parameters that are mostly the same as those you can pass to an SB3 agent constructor (see [SB3 Documentation](https://stable-baselines3.readthedocs.io/en/master/modules/base.html) under RL Algorithms).
## GPU Support
To Train with GPU Support set in the [config file](./config.yaml) ```prefer_device: "cuda"```.

This is an experimental feature and the system this app runs on needs to support CUDA. Your PyTorch and CUDA version got to match.
See if your system supports CUDA and get your CUDA version in the top right corner of the output:
```
nvidia-smi
```
Get the matching [PyTorch version](https://pytorch.org/get-started/locally/).