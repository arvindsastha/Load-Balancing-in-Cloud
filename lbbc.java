/**
 * 
 */
package org.cloudbus.cloudsim.examples;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * @author Arvind
 *
 */
public class lbbc {

	/**
	 * @param args
	 */
	
	static DatacenterBroker db = null;
	static List<Cloudlet> cloudletList1;
	static List<Double> R = new ArrayList<Double>();
	
	public static void main(String[] args) {
		//1.0: Initialize the CloudSim package. 
		//It should be called before creating any entities.
		int numUser=1;
		Calendar cal = Calendar.getInstance();
		boolean traceFlag=false;
		CloudSim.init(numUser, cal, traceFlag);
		
		//2.0: Create Datacenter: Datacenter --> Datacentercharacteristics --> HostList --> Processing element List
		//Also Defines policy for VM allocation and scheduling
		
		@SuppressWarnings("unused")
		Datacenter dc = createDatacenter();
		//3.0: Create Broker
		
		try { 
			db = new DatacenterBroker("Datacenter_Broker_1");
		} catch(Exception e){
			e.printStackTrace();
		}
		
		
		//4.0: Create Cloudlets:Defines the workload
		//created in a separate function just as datacenter before
		List<Cloudlet> cloudletList = createCloudlet();
		
		/*int cloudletId;
		long cloudletLength=40000;
		int pesNumber=1;
		long cloudletFilesize=300;
		long cloudletOutputsize=400;//Input + Output = Rmem (700) 
		UtilizationModelFull uti = new UtilizationModelFull();
		double alpha,beta;
		List<Double> R = new ArrayList<Double>();
		
		for(cloudletId=0;cloudletId<50;cloudletId++){
			
			Random r = new Random();
			long Rc = cloudletLength+r.nextInt(1000);
			alpha = (double)((double)Rc)/(((double)Rc)+700);
			beta = (double)700/(((double)Rc)+700);
			Cloudlet c = new Cloudlet(cloudletId,Rc,
					pesNumber,cloudletFilesize,cloudletOutputsize,uti,uti,uti);
			c.setUserId(db.getId());
			cloudletList.add(c);
			R.add((alpha*700)+(beta*Rc));
		}*/
			
		//5.0: Create VMs:Define the procedure for Task scheduling algorithm
		List<Vm> vmList = new ArrayList<Vm>();
		
		long diskSize = 20000;
		int ram = 2000;
		int mips = 1000;
		int bw = 1000;
		int vCPU = 1;
		String vmm = "XEN";
		
		for(int i=0;i<10;i++){
			
			Vm v = new Vm(i,db.getId(),mips,vCPU,ram,bw,diskSize,vmm,
					new CloudletSchedulerTimeShared());//space shared can be changed here
			//CloudletSchedulerSpaceShared() module is available
			vmList.add(v);
		}
		
		db.submitCloudletList(cloudletList);
		db.submitVmList(vmList);
		//6.0: Starts the simulation: Automated process, handled through event simulation engine
		CloudSim.startSimulation();
		
		List<Cloudlet> finalList = db.getCloudletReceivedList();
		
		CloudSim.stopSimulation();
		
		//7.0: Print results when simulation is over as Outputs
		int c=0;
		for(Cloudlet f : finalList){
			Log.printLine("Result of CloudLet No." + c);
			Log.printLine("***************************");
			Log.printLine("ID:" + f.getCloudletId() + ",Vm ID:" + f.getVmId() 
			+ ",VM Status:" + f.getCloudletStatusString() + ",Execution Time:" 
			+ f.getActualCPUTime() + ",Start Time:" + f.getExecStartTime() 
			+ ",Finish Time:" + f.getFinishTime() );
			c++;	
		}
		}

	public static Datacenter createDatacenter(){
		
		List<Pe> peList = new ArrayList<Pe>();
		
		PeProvisionerSimple pProvisioner = new PeProvisionerSimple(1000);
		
		Pe core1 = new Pe(0,pProvisioner);
		peList.add(core1);
		Pe core2 = new Pe(1,pProvisioner);
		peList.add(core2);
		
		List<Host> hostList = new ArrayList<Host>();
		
		int ram = 8000;
		int bw = 8000;
		long storage = 100000;
		
		for(int i=0;i<100;i++)
		{
			Host h = new Host(i,new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
					storage, peList, new VmSchedulerSpaceShared(peList));
			hostList.add(h);
		}
		
		String architecture = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double tz = 5.0;
		double cs = 3.0;
		double cm = 1.0;
		double cps = 0.05;
		double cbw = 0.10;
		DatacenterCharacteristics dcChar = new DatacenterCharacteristics(architecture,os,vmm,hostList,tz,cs,cm,cps,cbw); 
 		Datacenter dc =null;
 		LinkedList<Storage> SANStorage = new LinkedList<Storage>();
 		try {
			dc = new Datacenter("Datacenter1",dcChar,new myallocation(hostList),SANStorage,1);
			//myallocation is my own allocation where lbbc allocation is to be given 
			//once it is finished 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dc;
	}
	
	public static List<Cloudlet> createCloudlet()
	{
		cloudletList1 = new ArrayList<Cloudlet>();
		int cloudletId;
		long cloudletLength=40000;
		int pesNumber=1;
		long cloudletFilesize=300;
		long cloudletOutputsize=400;//Input + Output = Rmem (700) 
		UtilizationModelFull uti = new UtilizationModelFull();
		double alpha,beta;
		for(cloudletId=0;cloudletId<50;cloudletId++){
			
			Random r = new Random();
			long Rc = cloudletLength+r.nextInt(1000);
			alpha = (double)((double)Rc)/(((double)Rc)+700);
			beta = (double)700/(((double)Rc)+700);
			Cloudlet c = new Cloudlet(cloudletId,Rc,
					pesNumber,cloudletFilesize,cloudletOutputsize,uti,uti,uti);
			c.setUserId(db.getId());
			cloudletList1.add(c);
			R.add((alpha*700)+(beta*Rc));
		}
		return cloudletList1;
	}

}
