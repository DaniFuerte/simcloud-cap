package apartado4;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

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
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Apartado4 {

	
	public static void main(String [] args) {
		
		Log.printLine("Arrancando apartado 4...");
		
		// Paso 1 -> Inicializamos CloudSim
		CloudSim.init(1, Calendar.getInstance(), false);
		
		// Paso 2 -> Creación del Datacenter
		Datacenter dataCenter = createDatacenter("Datacenter_ap4");
		if(dataCenter != null) {
			Log.printLine("Creado Centro de datos -> "+dataCenter.getName());
		}
		
		// Paso 3 -> Creación de Broker
		DatacenterBroker broker = createBroker();
		
		// Paso 4 -> Creación de VMS 
		createVMS(broker);
		
		// Paso 5 -> Creacion de CLOUDLET
		createCloudlet(broker);
		
		// Paso 6 -> Simulación
		CloudSim.startSimulation();
		CloudSim.stopSimulation();
		
		// Paso 7 -> Recopilar resultados
		List<Cloudlet> resultados = broker.getCloudletReceivedList();
		printCloudletList(resultados);
		Log.printLine("Finalizado apartado 4!");
		
		
	}
	
	
	/*
	 * Creación del centro de datos
	 */
	
	@SuppressWarnings("unchecked")
	private static Datacenter createDatacenter(String name) {
		
		/* Datos */
		final int HOSTS = 3;
		int mips = 1200;
		int ram = 16384; //16gb*1021,75(deberia ser 1024, pero si haces la division...
		long almacenamiento = 1000000; // 1TB
		long bw = 10000; // Ancho de banda 10Gbps
		
		List<Pe>[] cpusHost = new List[HOSTS];
		Host[] hosts = new Host[HOSTS];
		List<Host> listHosts = new ArrayList<Host>();
		
		for(int i = 0;i < HOSTS;i++) {
			cpusHost[i] = new ArrayList<Pe>();
			cpusHost[i].add(new Pe(0,new PeProvisionerSimple(mips)));
			
			if(i == 1) {
				for(int j = i; j <= HOSTS;j++) 
					cpusHost[i].add(new Pe(j,new PeProvisionerSimple(mips)));
				
			}
			
			hosts[i] = new Host(i, new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw), almacenamiento,
					cpusHost[i], new VmSchedulerTimeShared(cpusHost[i]));
			
			listHosts.add(hosts[i]);

		}

		// especificaciones del centro de datos
		/*
		 * 0 -> Arquitectura
		 * 1 -> OS
		 * 2 -> VMM
		 */
		String [] especificaciones = {"x86", "Linux", "Xen"};
		/*
		 * 0 -> zona horaria
		 * 1 -> coste por segundo
		 * 2 -> coste por usuo de memoria
		 * 3 -> coste por almacenamiento
		 * 4 -> coste por ancho de banda
		 */
		double [] costes_zonaHoraria = {3.0,0.007,0.005,0.003,0.002};
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		
		DatacenterCharacteristics caracteristicas = new DatacenterCharacteristics(
				especificaciones[0],especificaciones[1],especificaciones[2],listHosts,costes_zonaHoraria[0],
				costes_zonaHoraria[1],costes_zonaHoraria[2],costes_zonaHoraria[3], costes_zonaHoraria[4]);
		
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, caracteristicas, new VmAllocationPolicySimple(listHosts),storageList,0);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return datacenter;
		
	}
	
	/*
	 * Creación de VMS && CLOUDLET
	 */
	
	// 1 -> Broker
	
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return broker;
	}
	
	// 2 -> VMS
	
	private static void createVMS(DatacenterBroker broker) {
		
		Log.printLine("Creando VMS...");
		final int VMS = 6;
		//DatacenterBroker broker = createBroker();
		int brokerId = broker.getId();
		
		List<Vm> vms = new ArrayList<Vm>();
		
		CloudletSchedulerTimeShared scheduler = new CloudletSchedulerTimeShared();
		
		for(int i = 0;i < VMS;i++) 
			vms.add(new Vm(i,brokerId,400,1,(int)(long)Math.round(2*1021.75), 1000, 40000, "Xen", scheduler));
		
		broker.submitVmList(vms);
		
		//return broker;
	}
	
	// 3 -> CLOUDLET
	
	private static void createCloudlet(DatacenterBroker broker) {
		
		final int CLOUDLET = 6;
		
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		Cloudlet cloudlet;
		
		for(int i = 0;i < CLOUDLET;i++) {
			cloudlet = new Cloudlet(0,00000, 1, 300, 300, 
					new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
			cloudlet.setUserId(broker.getId());
			cloudlet.setVmId(i);
			cloudletList.add(cloudlet);
		}
		
		broker.submitCloudletList(cloudletList);

	}
	
	
	/*Recopilar resultados*/
	private static void printCloudletList(List<Cloudlet> list) {
			
			String indent = "    ";
			Log.printLine();
			Log.printLine("========== OUTPUT ==========");
			Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
					+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
					+ "Start Time" + indent + "Finish Time");
		
			DecimalFormat dft = new DecimalFormat("###.##");
			
			for(Cloudlet cloudlet: list) {
				Log.print(indent + cloudlet.getCloudletId() + indent + indent);
		
				if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
					Log.print("SUCCESS");
		
					Log.printLine(indent + indent + cloudlet.getResourceId()
							+ indent + indent + indent + cloudlet.getVmId()
							+ indent + indent
							+ dft.format(cloudlet.getActualCPUTime()) + indent
							+ indent + dft.format(cloudlet.getExecStartTime())
							+ indent + indent
							+ dft.format(cloudlet.getFinishTime()));
				}
			}
			
	}
	
	
	
}
