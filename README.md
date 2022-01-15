# CS520-Imitation-Game

SUMMARY:

The purpose of this project is to train an agent (rather than implementing a specific decision agent) to imitate the agents we’ve build previously. For each of Project 1 and Project 2, take an agent of your choice, and build an ML agent to mimic it in the following way:

• Take the input data to be state descriptions of the current knowledge of the gridworld.

• Take the output data to be the corresponding actions the agent takes in the input states.

• The ML agent should consist of a neural network that maps from the input space to the output space (or, as necessary, modified, i.e. for softmax/probability regression).

• Train your model on data generated by the agent of your choice, over many episodes and many gridworlds.

After sufficient training, the ML agent can run by simulating the appropriate gridworld and using the trained network to make action selections given the current state. Repeatedly simulating the original agent and the ML agent on identical gridworlds will allow a comparison of performance between the two.

For this project, you’re welcome to use Tensorflow or PyTorch or whatever machine learning framework you see fit, or roll your own if you are inclined. All code must be your own, and commented sufficiently well as to indicate you understand what the underlying framework is doing. Half this project is building an agent to mimic an agent from Project 1, half this project is building an agent to mimic an agent from Project 2.
