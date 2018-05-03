package apartado6;

import java.util.Calendar;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import apartado5.ap5;

public class ap6 {

	private static final int USERS = 3;
	
	private static final int CPU1 = 2;
	private static final int HOST1 = 16;
	
	private static final int CPU2 = 4;
	private static final int HOST2 = 4;
	
	private static final int TYPEA = 8;
	private static final int TYPEB = 16;
	private static final int TYPEC = 24;
	
	@SuppressWarnings("unused")
	public static void main(String [] args) {
		
		Log.printLine("Arrancando apartado 6...");
		
		try {
			CloudSim.init(USERS, Calendar.getInstance(), false);
			
			// Paso 1 -> Creamos el Datacenter
			Datacenter datacenter = createDatacenter("Datacenter_ap6");
			
			// Paso 2 -> Creamos tantos brokers como usuarios tengamos
			DatacenterBroker [] brokers = new DatacenterBroker[USERS];
			int [] brokerIds = new int [USERS];
			for(int i = 0;i < USERS;i++) {
				brokers[i] = createBroker();
				brokerIds[i] = brokers[i].getId();
			}
			
			
			/*
			 * Paso 3 -> Creamos el VMS
			 * 
			 * Tipo A
			 * Tipo B
			 * Tipo C 
			 * 
			 */
			
			// Tipo A
			int mips = 2400;
			long almacenamiento = 120000; //120 GB
			int ram = 3072; //3GB
			long anchoBanda = 1000; //1GB
			int numProcesadores = 1;
			String vmm = "Xen";
			List<Vm> vmlistA = new ArrayList<Vm>();
			
			for (int i=0; i<TYPEA; i++){
				Vm vm = new Vm(i, brokerIds[0], mips, numProcesadores, ram, anchoBanda, almacenamiento, vmm, new CloudletSchedulerTimeShared());
				vmlistA.add(vm);
			}
			brokers[0].submitVmList(vmlistA);
			
			//Tipo B
			mips = 2000;
			almacenamiento = 80000; //80 GB
			ram = 2048; //2GB
			List<Vm> vmlistB = new ArrayList<Vm>();
			
			for (int i=0; i<TYPEB; i++){
				Vm vm = new Vm(i, brokerIds[1], mips, numProcesadores, ram, anchoBanda, almacenamiento, vmm, new CloudletSchedulerTimeShared());
				vmlistB.add(vm);
			}
			brokers[1].submitVmList(vmlistB);
			//Tipo C
			mips = 1800;
			almacenamiento = 60000; //60 GB
			ram = 1024; //1GB
			List<Vm> vmlistC = new ArrayList<Vm>();
			
			for (int i=0; i<TYPEC; i++){
				Vm vm = new Vm(i, brokerIds[2], mips, numProcesadores, ram, anchoBanda, almacenamiento, vmm, new CloudletSchedulerTimeShared());
				vmlistC.add(vm);
			}
			brokers[2].submitVmList(vmlistC);
			
			// Simulación
			CloudSim.startSimulation();
			
			CloudSim.stopSimulation();
			
			printBrokersResults(brokers);
			
			Log.printLine("Apartado 6 terminado!");
			
		} catch(Exception e) {
			e.printStackTrace();
			Log.print("Se ha producido un error inesperado durante la simulación");
		}
		
		
	}
	
	private static List<Host> createHost(int ncpus, 
			int nhosts, int mips, int ram, long memory, int bw,
			String message) {
		
		Log.printLine(message);
		
		List<Host> listaHosts = new ArrayList<Host>();
		List<Pe> cpusHost = new ArrayList<Pe>();
		
		// creamos las cpus que va a tener el host
		
		for(int i = 0;i < ncpus;i ++) {
			cpusHost.add(new Pe(i,new PeProvisionerSimple(mips)));
		}
		
		Host [] host = new Host[nhosts];
		for(int i = 0;i < nhosts;i++) {
			host[i] = new Host(i,new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),memory, cpusHost,
					new VmSchedulerTimeShared(cpusHost));
			listaHosts.add(host[i]);
		}
		
		
		return listaHosts;
		
	}
	
	private static Datacenter createDatacenter(String name) {
		
		List<Host> listaHosts = new ArrayList<Host>();
		Log.printLine("Creando centro de datos " + name);
		
		// creamos host de tipo 1
		List<Host> host1 = createHost(CPU1,HOST1, 2000, 8192, 1000000, 1000, "Creando Host de tipo 1...");
		// creamos host de tipo 2
		List<Host> host2 = createHost(CPU2,HOST2, 2400, 8192, 2000000, 1000, "Creando Host de tipo 2...");
		
		// Agregamos tanto los host de tipo 1, como de tipo 2 a la lista de host
		listaHosts.addAll(host1);
		listaHosts.addAll(host2);
		
		if(!listaHosts.isEmpty()) {
			Log.printLine("Agregados hosts de tipo 1 y 2 a la lista de host");
			
			/*
			 * 0 -> arquitectura
			 * 1 -> os
			 * 2 -> vmm
			 */
			
			String [] caracteristicas = {"x86","Linux","Xen"};
			double zonaHoraria = 1.0; // M -> ESP
			/*
			 * 0 -> costes por segundo
			 * 1 -> costes por memoria
			 * 2 -> costes por almacenamiento
			 * 3 -> costes por ancho de banda
			 */
			double [] costes = {0.01,0.01,0.01,0.01};
			
			LinkedList<Storage> storageList = new LinkedList<Storage>();
			
			// Construímos las caracteristicas del futuro centro de datos
			DatacenterCharacteristics datacenterCharacteristics = new DatacenterCharacteristics(caracteristicas[0],
					caracteristicas[1],caracteristicas[2],listaHosts,zonaHoraria,costes[0], costes[1], costes[2], costes[3]);
			
			Datacenter datacenter = null;
			try {
				datacenter = new Datacenter(name, datacenterCharacteristics, new ap5(listaHosts), storageList, 0);
				return datacenter;
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
		} 
		
		return null;
		
	}
	
	private static DatacenterBroker createBroker() {
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker("Broker");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return broker;
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
	
	private static void printBrokersResults(DatacenterBroker [] brokers) {
		for(int i = 0;i < brokers.length;i++) {
			List<Cloudlet> results = brokers[i].getCloudletReceivedList();
			printCloudletList(results);
		}
	}
	
	
}
