/**
 * 
 */
package scheduling;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import utils.Debug;

/**
 * Handles the simulation of a scheduler
 * @author NeilDG
 *
 */
public class SchedulerSimulation {
	private final static String TAG = "MainActivity";
	
	/*
	 * Util function that arranges an array P by arrival time and converts it into a queue.
	 */
	public static Queue<ProcessRep> arrangeByArrivalTime(ProcessRep[] P) {
		Arrays.sort(P, new ProcessRep.ArrivalSorter());		
		Queue<ProcessRep> pQueue = new LinkedList<ProcessRep>();
		for(int i = 0; i < P.length; i++) {
			Debug.log(TAG, "P["+P[i].getID()+"] Arrival Time: " +P[i].getArrivalTime()+ " Exec Time: " +P[i].getExecutionTime()+ " Priority: " +P[i].getPriority());
			pQueue.add(P[i]);
		}
		
		return pQueue;
	}
	
	public void start(String[] inputs) {
		//parse inputs by representing as ProcessRep
	}
	
	//Should be called from a main thread.
	public void startSimulation() {
		ProcessRep[] P = new ProcessRep[8];
		
		for(int i = 0; i < P.length; i++) {
			P[i] = ProcessRep.generateRandomData(i);
		}
		
		Queue<ProcessRep> pQueue = arrangeByArrivalTime(P);
		//this.performFCFS(pQueue);
		
		pQueue = arrangeByArrivalTime(P);
		//this.performShortestJobFirst(pQueue, true);
		
		pQueue = arrangeByArrivalTime(P);
		//this.performRoundRobin(pQueue, 6);
		
		P = new ProcessRep[10];
		P[0] = new ProcessRep(1, 0, 7, 2);
		P[1] = new ProcessRep(2, 1, 11, 3);
		P[2] = new ProcessRep(3, 2, 6, 3);
		P[3] = new ProcessRep(4, 3, 5, 3);
		P[4] = new ProcessRep(5, 4, 5, 1);
		P[5] = new ProcessRep(6, 6, 2, 3);
		P[6] = new ProcessRep(7, 7, 3, 2);
		P[7] = new ProcessRep(8, 8, 6, 1);
		P[8] = new ProcessRep(9, 9, 3, 2);
		P[9] = new ProcessRep(10, 10, 3, 2);
		
		pQueue = arrangeByArrivalTime(P);
		this.performFCFS(pQueue);
		
		pQueue = arrangeByArrivalTime(P);
		this.performShortestJobFirst(pQueue, true);
		
		pQueue = arrangeByArrivalTime(P);
		this.performPriorityShortestJobFirst(pQueue, true);
		
		pQueue = arrangeByArrivalTime(P);
		this.performRoundRobin(pQueue, 5);
	}
	
	private void performFCFS(Queue<ProcessRep> P) {
		//simulation here
		int cpuTime = 0;
		
		Queue<ProcessExecutor> readyQueue = new LinkedList<ProcessExecutor>();
		LinkedList<ProcessExecutor> finishedP = new LinkedList<ProcessExecutor>();
		ProcessExecutor current = null; //process in cpu
		
		while(!P.isEmpty() || !readyQueue.isEmpty() || current != null) {
			
			while(!P.isEmpty() && P.peek().getArrivalTime() == cpuTime){
				ProcessExecutor e = ProcessExecutor.createExecutor(P.remove());
				//e.reportReadyQueueEntry(cpuTime); //should only be called on a second and succeeding entries to ready queue
				readyQueue.add(e);
			}
			
			if(!readyQueue.isEmpty() && current == null) {
				current = readyQueue.remove();
				current.reportCPUEntry(cpuTime);
			}
			
			//simulation of CPU execution
			if(current != null) {
				current.execute();
				if(current.hasExecuted()) {
					current.reportFinished(cpuTime + 1);
					finishedP.add(ProcessExecutor.makeFinishedCopy(current));
					current = null;
				}
			}
			cpuTime++;
		}
		
		
		Debug.log(TAG, "=====FCFS FINISHED SIMULATION. EXECUTION ORDER=====");
		//printing of results
		for(int i=0; i < finishedP.size(); i++) {
			ProcessExecutor f = finishedP.get(i);
			f.computeWaitingTime();
			System.out.println("P[" + f.getID() + "] " +f.getTimeString()+ " Waiting Time: " + f.getWaitingTime());
		}
		
		System.out.println("Average waiting time: " +ProcessExecutor.computeAVGWaitingTime((LinkedList<ProcessExecutor>) finishedP));
	}
	
	private void performShortestJobFirst(Queue<ProcessRep> P, boolean isPreemptive) {
		//simulation here
		int cpuTime = 0;
		int min = 0;
		
		LinkedList<ProcessExecutor> readyQueue = new LinkedList<ProcessExecutor>();
		LinkedList<ProcessExecutor> finishedP = new LinkedList<ProcessExecutor>();
		ProcessExecutor current = null; //process in cpu
		
		while (!P.isEmpty() || !readyQueue.isEmpty() || current != null) {
			
			while(!P.isEmpty() && P.peek().getArrivalTime() == cpuTime) {
				ProcessExecutor e = ProcessExecutor.createExecutor(P.remove());
				readyQueue.add(e);
			}
			
			min = this.getMinimumTime(readyQueue);
			
			if(!readyQueue.isEmpty()) {
				if(current==null) {
					current = readyQueue.remove(min);
					current.reportCPUEntry(cpuTime);
				}
				else if(readyQueue.get(min).getRemainingTime() < current.getRemainingTime() && isPreemptive) {
					current.reportReadyQueueEntry(cpuTime);
					readyQueue.add(current);
					current = readyQueue.remove(min);
					current.reportCPUEntry(cpuTime);
				}
			}
			
			if(current != null) {
				current.execute();
				if(current.hasExecuted()) {
					current.reportFinished(cpuTime + 1);
					finishedP.add(ProcessExecutor.makeFinishedCopy(current));
					current = null;
				}
			}
			cpuTime++;
		}
		
		Debug.log(TAG, "=====SJF FINISHED SIMULATION. EXECUTION ORDER=====");
		//printing of results
		for(int i=0; i < finishedP.size(); i++) {
			ProcessExecutor f = finishedP.get(i);
			f.computeWaitingTime();
			System.out.println("P[" + f.getID() + "] " +f.getTimeString()+ " Waiting Time: " + f.getWaitingTime());
		}
		
		System.out.println("Average waiting time: " +ProcessExecutor.computeAVGWaitingTime((LinkedList<ProcessExecutor>) finishedP));
	}
	
	private void performPriorityShortestJobFirst(Queue<ProcessRep> P, boolean isPreemptive) {
		//simulation here	
		Debug.log(TAG, "=====PRIORITY SJF FINISHED SIMULATION. EXECUTION ORDER=====");
		//printing of results
	}
	
	private void performRoundRobin(Queue<ProcessRep> P, int timeSlice) {
		//simulation here	
		Debug.log(TAG, "=====ROUND-ROBIN FINISHED SIMULATION. EXECUTION ORDER=====");
		//printing of results
	}
	
	private int getMinimumTime(LinkedList<ProcessExecutor> P) {
		int min = 0;
		
		for(int i = 1; i < P.size(); i++) {
			if(P.get(i).getRemainingTime() < P.get(min).getRemainingTime()) {
				min = i;
			}
			else if(P.get(i).getRemainingTime() == P.get(min).getRemainingTime() && P.get(i).getArrivalTime() <= P.get(min).getArrivalTime()) {
				min = i;
			}
		}
		
		return min;
	}
	
	
}
