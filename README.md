
# Load-Balancing-in-Cloud using StackelBerg's model

## Introduction ##
The main problem in the current scenario is that most physical hosts in the cloud data centre are so much overloaded that it makes the entire cloud data
centre’s load imbalanced. The existing load balancing approaches have relatively high complexity, high failure of requests. This paper has focused on how the physical hosts for deploying requested tasks is selected based on the
requirement of the request and made us of Stackelberg’s approach. The algorithms and related works that has been done previously utilize a series of
algorithms through optimizing the candidate target hosts within an algorithm cycle and then picking out the optimal target hosts to achieve the immediate
load balancing effect. The proposed model in this work makes a decision of which physical host must be allocated to which request based on the request.
The model uses First-in-First-out for task assignments. Simulation results compared with the existing works show that the proposed approach has reduced
the failure number of task deployment events obviously, improved the throughput, and optimized external services performance of cloud data centres.

## Algorithm of proposed system ##
The algorithm consists of 3 phases
1. INITIAL SETUP PHASE
2. CLUTSERING PHASE
3. ASSIGNEMENT/MAPPING PHASE

#### 1] Setup Phase ####
Follower’s informing the leader about their status thus giving the leader an advantage. In this phase, all nodes will send their LOAD DEGREE to the
Load balancer. Here the followers are the host and the leader is the load balancer. Load degree is calculated based on the left over memory and left over
CPU cycles of the host. The load balancer will calculate an average load degree based on those values and will select the hosts which are above the average value for the next phase. 
Once the hosts are selected, they will be evaluated for their fitness using following function that includes transmission delay. Only those nodes above a threshold will be taken to the next stage.

#### 2] Clustering Phase ####
Leader announcing the best optimal strategy. The hosts selected above will be in a set H. Each hosts will have their corresponding load degree in the set.
Based on the load degree, 20 nodes (20%) are selected as Cluster Heads (CH). These 20 nodes are removed from H and put into a new set CH. Now that we have cluster heads, clustering can be initiated. 
The clustering is done based on the similarity measure between cluster heads and other nodes. Finally we will have 20 clusters with varying load degrees. The similarity measure function are stated below.
Maximizing the utility function will solve the follower problem as stated below. Based on the cost, the clusters are arranged in order for them to be selected. 
In case of any discrepancy followers will respond to the leader about that. Leader will select the best optimal strategy according to that.

#### 3] Mapping Phase ####
Let be the set of clusters with weights representing the load degree of the clusters.

Let be the set of hosts within the cluster C i with weights representing the load degree (used up memory and CPU cycles) of the hosts.

Let be the set of tasks with weights representing the required CPU and Memory by the task.

Generally, in cloud systems. Assignment is done from

**Step 1:** Choosing the best nearest cluster for the optimal solution.

**Step 2:** Choosing the best nearest host within the cluster for the optimal solution.

The chosen hosts are put into a set H′ which will be the answer set.
Then FIFO assignment of tasks to hosts is followed.
Maximizing this set R will solve the throughput problem of the leader.

## System Model ##
First, the Cloud Controller acquires the information of resource
requested by users’ tasks and the status information of remaining resource
amount (including CPU and memory) of available physical hosts in cloud data
centre. By using the information acquired from Cloud Controller, the algorithm
proposed generates the deployment strategy, which is transmitted to
Deployment Controller whose function is to control and carry out the
deployment of requested tasks

**1.** Followers first announce their load degree to Leader: The followers will initially announce their load degree to the followers, i.e., a series of hosts
h = [h1, h2,...,hN].

**2.** Leader delivers the best optimal strategy: When informed of the follower’s load degree L.D, the leader selects the best-response strategy from its
strategy set available and will denote it as the best-response strategy

**3.** Leader changes its optimal strategy based on the identified best response strategies of the followers: Based on the identified best-response
strategies x1(h), . . . ,xN(h) of each follower, the leader will select an optimal strategy
