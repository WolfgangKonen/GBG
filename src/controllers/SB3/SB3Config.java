package controllers.SB3;

import java.util.LinkedList;
import java.util.List;

public class SB3Config {
    //Connection Config
    public static final int PORT = 8094;
    public class SB3Server {
        public static final int PORT = 8095;
        public static final String IP_ADDRESS = "127.0.0.1";
        public static final String HOST = "http://" + IP_ADDRESS + ":" + Integer.toString(PORT);
    }

    // Default params for BaseParameters
    public static final String DEFAULT_AGENT = "DQN";
    public static final String[] DEFAULT_AGENT_OPTIONS = {"DQN", "PPO", "MPPO"};
    public static final boolean DEFAULT_SELF_PLAY = true;
    public static final int DEFAULT_TRAIN_TIME_STEPS = 100; // 10000;
    public static final boolean DEFAULT_USE_STANDARD_SB3_PARMAS = true;
    public static final boolean DEFAULT_USE_STANDARD_NETWORK_PARAMS = true;
    public static final boolean DEFAULT_USE_ENEMIES = false;

    public static final double DEFAULT_LEARNING_RATE = 0.0001;
    public static final int DEFAULT_STATS_WINDOW_SIZE = 100;
    public static final String DEFAULT_TENSORBOARD_LOG = "logs";
    public static final int DEFAULT_VERBOSE = 0;
    public static final Integer DEFAULT_SEED = null;
    public static final String DEFAULT_DEVICE = "auto";

    // tool tips for base parameters
    public static final String TIP_AGENT = "Select the agent type (e.g., DQN, PPO, MPPO).";
    public static final String TIP_SELF_PLAY = "Enable self-play for training.";
    public static final String TIP_TRAIN_TIMESTEPS = "The total number of timesteps for training.";
    public static final String TIP_STANDARD_SB3_PARAMS = "Enable standard Stable-Baselines3 parameters for training.";
    public static final String TIP_STANDARD_NETWORK_PARAMS = "Enable standard neural network parameters for training.";
    public static final String TIP_LEARNING_RATE = "Learning rate for the optimizer. Can be a fixed value or a function.";
    public static final String TIP_STATS_WINDOW_SIZE = "Window size for rollout logging (e.g., success rate, episode length, mean reward).";
    public static final String TIP_TENSORBOARD_LOG = "The log location for TensorBoard. Set to None to disable logging.";
    public static final String TIP_VERBOSE = "Verbosity level: 0 for no output, 1 for info, 2 for debug messages.";
    public static final String TIP_SEED = "Seed for pseudo-random number generators to ensure reproducibility.";
    public static final String TIP_DEVICE = "The computing device to use (e.g., CPU or GPU).";

    public static class EnemyAgentsDefaultValues {
        public static final String[] DEFAULT_ENEMIES = {
                "Self Play",
                "Max-N",
                "RHEA-SI",
                "Random",
                "MCTS",
                "MC-N"
        };
    }

    // Default params for DQN
    public static class DQNDefaultValues {
        public static final int DEFAULT_BUFFERSIZE = 1000000;
        public static final int DEFAULT_LEARNING_STARTS = 100;
        public static final int DEFAULT_BATCH_SIZE = 32;
        public static final double DEFAULT_TAU = 1.0;
        public static final double DEFAULT_GAMMA = 0.99;
        public static final int DEFAULT_TRAIN_FREQ = 4;
        public static final int DEFAULT_GRADIENT_STEPS = 1;
        public static final boolean DEFAULT_OPTIMIZE_MEMORY_USAGE = false;
        public static final int DEFAULT_TARGET_UPDATE_INTERVAL = 10000;
        public static final double DEFAULT_EXPLORATION_FRACTION = 0.1;
        public static final double DEFAULT_EXPLORATION_INITIAL_EPS = 1.0;
        public static final double DEFAULT_EXPLORATION_FINAL_EPS = 0.05;
        public static final double DEFAULT_MAX_GRAD_NORM = 10.0;

        // Tool tips from sb3 documentation
        public static final String TIP_BUFFER_SIZE = "Size of the replay buffer.";
        public static final String TIP_LEARNING_STARTS = "How many steps of the model to collect transitions for before learning starts.";
        public static final String TIP_BATCH_SIZE = "Minibatch size for each gradient update.";
        public static final String TIP_TAU = "The soft update coefficient ('Polyak update', between 0 and 1). Default is 1 for hard update.";
        public static final String TIP_GAMMA = "The discount factor.";
        public static final String TIP_TRAIN_FREQ = "Update the model every train_freq steps or specify as a tuple (e.g., (5, 'step') or (2, 'episode')).";
        public static final String TIP_GRADIENT_STEPS = "How many gradient steps to do after each rollout. -1 means as many gradient steps as steps done in the environment.";
        public static final String TIP_OPTIMIZE_MEMORY_USAGE = "Enable a memory-efficient replay buffer at a cost of more complexity.";
        public static final String TIP_TARGET_UPDATE_INTERVAL = "Update the target network every target_update_interval environment steps.";
        public static final String TIP_EXPLORATION_FRACTION = "Fraction of the entire training period over which the exploration rate is reduced.";
        public static final String TIP_EXPLORATION_INITIAL_EPS = "Initial value of random action probability.";
        public static final String TIP_EXPLORATION_FINAL_EPS = "Final value of random action probability.";
        public static final String TIP_MAX_GRAD_NORM = "Maximum value for gradient clipping.";
    }


    // Default params for PPO
    public static class PPODefaultValues {
        public static final int DEFAULT_N_STEPS = 2048;
        public static final int DEFAULT_BATCH_SIZE = 64;
        public static final int DEFAULT_N_EPOCHS = 10;
        public static final double DEFAULT_GAMMA = 0.99;
        public static final double DEFAULT_GAE_LAMBDA = 0.95;
        public static final double DEFAULT_CLIP_RANGE = 0.2;
        public static final Double DEFAULT_CLIP_RANGE_VF = null; // Use Double to allow null value
        public static final boolean DEFAULT_NORMALIZE_ADVANTAGE = true;
        public static final double DEFAULT_ENT_COEF = 0.0;
        public static final double DEFAULT_VF_COEF = 0.5;
        public static final double DEFAULT_MAX_GRAD_NORM = 0.5;
        public static final boolean DEFAULT_USE_SDE = false;
        public static final int DEFAULT_SDE_SAMPLE_FREQ = -1;
        public static final Double DEFAULT_TARGET_KL = null; // Use Double to allow null value

        // Tool Tips from sb3 documentation
        public static final String TIP_BATCH_SIZE = "The size of mini-batches for training. Must be a factor of n_steps * n_envs.";
        public static final String TIP_N_STEPS = "The number of steps to run for each environment per update. Batch size = n_steps * n_envs.";
        public static final String TIP_N_EPOCHS = "The number of epochs to perform optimization over each update. Higher values may lead to better performance.";
        public static final String TIP_GAMMA = "The discount factor for rewards. It determines the importance of future rewards compared to immediate rewards.";
        public static final String TIP_GAE_LAMBDA = "The factor for Generalized Advantage Estimation (GAE). It balances bias and variance in advantage estimates.";
        public static final String TIP_CLIP_RANGE = "The clipping parameter for PPO's objective. Limits policy updates to avoid large deviations.";
        public static final String TIP_CLIP_RANGE_VF = "Clipping parameter for the value function";
        public static final String TIP_NORMALIZE_ADVANTAGE = "Whether to normalize or not the advantage";
        public static final String TIP_ENT_COEF = "The coefficient for the entropy term in the loss function. Encourages exploration by penalizing low entropy.";
        public static final String TIP_VF_COEF = "The coefficient for the value function term in the loss function.";
        public static final String TIP_MAX_GRAD_NORM = "The maximum gradient norm for gradient clipping. Helps stabilize training by preventing exploding gradients.";
        public static final String TIP_USE_SDE  = "Whether to use generalized State Dependent Exploration (gSDE) instead of action noise exploration";
        public static final String TIP_SDE_SAMPLE_FREQ = "Sample a new noise matrix every n steps when using gSDE Default: -1 (only sample at the beginning of the rollout)";
        public static final String TIP_TARGET_KL = "The target KL divergence between the old and new policies. Early stopping occurs if the value is exceeded.";
    }

    public static class DefaultEvaluationOptions {
        public static final int DEFAULT_EVALUATE_EVERY_EPISODES = 5000;
        public static final int DEFAULT_NUMBER_GAMES = 300;
        public static final String[] DEFAULT_OPPONENTS = {
                "Random",
                "Max-N",
                "RHEA-SI",
                "MCTS",
                "MC-N"
        };

        public static final boolean DEFAULT_SAFE_BEST_MODEL = false;
    }



    //TODO put in class
    // Default params for NetworkParameters
    public static final int DEFAULT_NUM_NEURONS = 64;
    public static final List<Integer> DEFAULT_LAYERS = new LinkedList<Integer>();
    public static final String[] DEFAULT_ACTIVATION_FUNCTIONS= {"ReLU", "LeakyReLU", "Sigmoid", "Tanh"};
    public static final String DEFAULT_ACTIVATION_FUNCTION= "ReLU";

    public class SelfPlayDefaultParameters {
        public static final int DEFAULT_POLICY_WINDOW_SIZE = 5;
        public static final int DEFAULT_ADD_POLICY_EVERY_X_STEPS = 10000;
        public static final double DEFAULT_USE_LATEST_POLICY = 0.35;
    }
}
