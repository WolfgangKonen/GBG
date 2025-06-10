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
