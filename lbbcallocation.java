/**
 * 
 */
package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.lbbc;
/**
 * @author Arvind
 *
 */

//allocateHostforVM is the function to be implemented


public class lbbcallocation extends VmAllocationPolicy{

		
	    /** The vm table. */
		private Map<String, Host> vmTable;

		/** The used pes. */
		private Map<String, Integer> usedPes;

		/** The free pes. */
		private List<Integer> freePes;

		/**
		 * Creates the new VmAllocationPolicySimple object.
		 * 
		 * @param list the list
		 * @pre $none
		 * @post $none
		 */
		public lbbcallocation(List<? extends Host> list) {
				super(list);

			setFreePes(new ArrayList<Integer>());
			for (Host host : getHostList()) {
				getFreePes().add(host.getNumberOfPes());

			}

			setVmTable(new HashMap<String, Host>());
			setUsedPes(new HashMap<String, Integer>());
		}

		/**
		 * Allocates a host for a given VM.
		 * 
		 * @param vm VM specification
		 * @return $true if the host could be allocated; $false otherwise
		 * @pre $none
		 * @post $none
		 */
		@SuppressWarnings("null")
		@Override
		public boolean allocateHostForVm(Vm vm) {
			int requiredPes = vm.getNumberOfPes();
			boolean result = false;
			int tries = 0;
			List<Integer> freePesTmp = new ArrayList<Integer>();
			for (Integer freePes : getFreePes()) {
				freePesTmp.add(freePes);
			}

			if (!getVmTable().containsKey(vm.getUid())) { // if this vm was not created
				do {// we still trying until we find a host or until we try all of them
					int idx = -1;
					double max = Double.MIN_VALUE;
					
					List<Double> L = new ArrayList<Double>();
					//getting LmaxREQ
					for(int i=0;i<lbbc.R.size();i++){
						int res = Double.compare(max, lbbc.R.get(i));
						if(res<0){	
							max=lbbc.R.get(i);
						    idx=i;
						}
					}
					
					//creating Li list
					for(int i=0;i<getHostList().size();i++){
						double d = getHostList().get(i).getMaxAvailableMips();
						double d1 = getHostList().get(i).getStorage();
						double Lc = (double)d1/(d+d1);
						double Lmem = (double)d/(d+d1);
						L.add((Lc*d)+(Lmem*d1));
					}
					
					//creating NPH list
					List<Host> NPH = null;
					int m=0;
					for(int i=0;i<getHostList().size();i++){
						int res = Double.compare(L.get(i), max);
						if(res>0) {NPH.add(getHostList().get(i));m++;} 
					}
					
					//creating posterior probability list
					double pbi = (double) 1/m;
					double denom =0;
					for(int i=0;i<m;i++){
						double pabi = 1 - (max/L.get(NPH.get(i).getId()));
						denom += pabi*pbi;
					}
					List<Double> posterior = new ArrayList<Double>();
					for(int i=0;i<m;i++){
						double pabi = 1 - (max/L.get(NPH.get(i).getId()));
						double post = (pabi * pbi)/denom;
						posterior.add(post);
					}
						
					//choosing with max posterior probability 
					//as seed/cluster center
					double max1 = posterior.get(0);
					int idx2=0;
					for(int i=1;i<posterior.size();i++){
						int res = Double.compare(posterior.get(i), max1);
						if(res>0) {max1=posterior.get(i);idx2=i;}
					}
					
					//adding that chosen seed in NPHnew
					List<Host> NPHnew = new ArrayList<Host>();
					NPHnew.add(getHostList().get(idx2));
					
					
					double threshold = 287.0;//No proper Threshold info about threshold in base paper
					
					//creating NPHnew list with SD measure
					double pj = posterior.get(NPHnew.get(0).getId());
					double lcj = NPHnew.get(0).getMaxAvailableMips();
					double lmj = NPHnew.get(0).getStorage();
					for(int i=0;i<NPH.size();i++){
						Host h = NPH.get(i);
						double pi = posterior.get(h.getId());
						double lci = h.getMaxAvailableMips();
						double lmi = h.getStorage();
						double res = SD(pi,pj,lci,lcj,lmi,lmj);
						if(res>threshold){
							NPHnew.add(h);
						}	
					}
					
					
					//NPHnew should set as the default host list for allocation
					
					//Step 23 of algorithm
					
					Host host = getHostList().get(idx);
					result = host.vmCreate(vm);

					if (result) { // if vm were succesfully created in the host
						getVmTable().put(vm.getUid(), host);
						getUsedPes().put(vm.getUid(), requiredPes);
						getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
						result = true;
						break;
					} else {
						freePesTmp.set(idx, Integer.MIN_VALUE);
					}
					tries++;
				} while (!result && tries < getFreePes().size());

			}

			return result;
		}

		
		public double SD(double pi,double pj,double lci,double lcj,double lmi,double lmj){
			
			double res=0.0;
			res = ((pi-pj)*(pi-pj))+((lci-lcj)*(lci-lcj))+((lmi-lmj)*(lmi-lmj));
			res = (double) 1/res;
			return res;
		}
		/**
		 * Releases the host used by a VM.
		 * 
		 * @param vm the vm
		 * @pre $none
		 * @post none
		 */
		@Override
		public void deallocateHostForVm(Vm vm) {
			Host host = getVmTable().remove(vm.getUid());
			int idx = getHostList().indexOf(host);
			int pes = getUsedPes().remove(vm.getUid());
			if (host != null) {
				host.vmDestroy(vm);
				getFreePes().set(idx, getFreePes().get(idx) + pes);
			}
		}

		/**
		 * Gets the host that is executing the given VM belonging to the given user.
		 * 
		 * @param vm the vm
		 * @return the Host with the given vmID and userID; $null if not found
		 * @pre $none
		 * @post $none
		 */
		@Override
		public Host getHost(Vm vm) {
			return getVmTable().get(vm.getUid());
		}

		/**
		 * Gets the host that is executing the given VM belonging to the given user.
		 * 
		 * @param vmId the vm id
		 * @param userId the user id
		 * @return the Host with the given vmID and userID; $null if not found
		 * @pre $none
		 * @post $none
		 */
		@Override
		public Host getHost(int vmId, int userId) {
			return getVmTable().get(Vm.getUid(userId, vmId));
		}

		/**
		 * Gets the vm table.
		 * 
		 * @return the vm table
		 */
		public Map<String, Host> getVmTable() {
			return vmTable;
		}

		/**
		 * Sets the vm table.
		 * 
		 * @param vmTable the vm table
		 */
		protected void setVmTable(Map<String, Host> vmTable) {
			this.vmTable = vmTable;
		}

		/**
		 * Gets the used pes.
		 * 
		 * @return the used pes
		 */
		protected Map<String, Integer> getUsedPes() {
			return usedPes;
		}

		/**
		 * Sets the used pes.
		 * 
		 * @param usedPes the used pes
		 */
		protected void setUsedPes(Map<String, Integer> usedPes) {
			this.usedPes = usedPes;
		}

		/**
		 * Gets the free pes.
		 * 
		 * @return the free pes
		 */
		protected List<Integer> getFreePes() {
			return freePes;
		}

		/**
		 * Sets the free pes.
		 * 
		 * @param freePes the new free pes
		 */
		protected void setFreePes(List<Integer> freePes) {
			this.freePes = freePes;
		}

		/*
		 * (non-Javadoc)
		 * @see cloudsim.VmAllocationPolicy#optimizeAllocation(double, cloudsim.VmList, double)
		 */
		@Override
		public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.cloudbus.cloudsim.VmAllocationPolicy#allocateHostForVm(org.cloudbus.cloudsim.Vm,
		 * org.cloudbus.cloudsim.Host)
		 */
		@Override
		public boolean allocateHostForVm(Vm vm, Host host) {
			if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
				getVmTable().put(vm.getUid(), host);

				int requiredPes = vm.getNumberOfPes();
				int idx = getHostList().indexOf(host);
				getUsedPes().put(vm.getUid(), requiredPes);
				getFreePes().set(idx, getFreePes().get(idx) - requiredPes);

				Log.formatLine(
						"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
						CloudSim.clock());
				return true;
			}

			return false;
		}

}
