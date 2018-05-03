package apartado5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;

public class ap5 extends VmAllocationPolicy {

	/**
	protected ConcurrentHashMap<String, Host> vmTable;
	
	protected ConcurrentHashMap<String, Integer> usedPes;
	
	protected CopyOnWriteArrayList<Integer> freePes;
	**/
	
	/** The vm table. */
	private Map<String, Host> vmTable;

	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;
	
	
	public ap5(List<Host> list) {
		super(list);
		
		this.freePes = new ArrayList<Integer>();
		
		for(Host host: this.getHostList()) {
			this.freePes.add(host.getNumberOfPes());
		}
		
		this.vmTable = new HashMap<String, Host>();
		this.usedPes = new HashMap<String, Integer>();
		
	}
	
	
	@Override
	public boolean allocateHostForVm(Vm vm) {
		int requiredPes = vm.getNumberOfPes();
		boolean result = false;
		// coger la lista de hosts temporal para modificarla
		List<Host> listaHosts = new ArrayList<>();
		listaHosts.addAll(getHostList());

		/** lista de random usados */
		List<Integer> randomList = new ArrayList<>();
		// generar el random
		Integer random = Integer.valueOf((int) (Math.random() * (listaHosts
				.size())));

		randomList.add(random);
		Host host;
		if (!vmTable.containsKey(vm.getUid())) { // si no existe la vm dentro del host
			do {// lo intentaremos hasta encontrar host, o hasta haberlo intentado con todos ellos

				host = listaHosts.get(random);
				result = host.vmCreate(vm);

				if (result) { // Si la vm fue creada dentro del host
					vmTable.put(vm.getUid(), host);
					usedPes.put(vm.getUid(), requiredPes);
					freePes.set(random,
							freePes.get(random) - requiredPes);
					result = true;
					break;
				} else {
					// randomList.remove(random);
					random = Integer.valueOf((int) (Math.random() * (listaHosts
							.size())));
					if (randomList.size() != listaHosts.size()){
						while (randomList.contains(random))
							random = Integer
									.valueOf((int) (Math.random() * (listaHosts
											.size())));
						randomList.add(random);
					} else 
						break;
				}
			} while (!result && !listaHosts.isEmpty());

		}

		return result;
	}
	
	
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		// TODO Auto-generated method stub
	
		/* Si la vm ha sido creada dentro del host*/
		if(host.vmCreate(vm)) {
			vmTable.put(vm.getUid(), host); // asocio UiD de vm al host
			int nPes = vm.getNumberOfPes(); 
			int index = this.getHostList().indexOf(host); // busco el host en la lista de hosts
			usedPes.put(vm.getUid(), nPes);// asocio la vm por su Uid al nPes que esta tiene
			freePes.set(index, (freePes.get(index)-nPes)); // modifico la lista de Pes libres
			Log.formatLine("%.2f: VM #" + vm.getId()
			+ " has been allocated to the host #" + host.getId(),
			CloudSim.clock());
			
			return true;
			
		}
		
		return false;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public void deallocateHostForVm(Vm vm) {
		// TODO Auto-generated method stub
		Host host = vmTable.remove(vm.getUid());
		int idx = getHostList().indexOf(host);
		int pes = usedPes.remove(vm.getUid());
		if (host != null) {
			host.vmDestroy(vm);
			freePes.set(idx, freePes.get(idx) + pes);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		// TODO Auto-generated method stub
		return vmTable.get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		// TODO Auto-generated method stub
		return vmTable.get(Vm.getUid(userId, vmId));
	}
	
	
	/** Métodos de Acceso **/
	public Map<String, Host> getVmTable() {
		return vmTable; 
	}
	
	public void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}
	
	public Map<String, Integer> getUsedPes() {
		return usedPes;
	}
	
	public void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}
	
	public List<Integer> getFreePes() {
		return freePes;
	}
	
	public void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}

}
