package apartado3;

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
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class Apartado3 {

	private static final int USERS = 4;
	private static final int VM = 3;
	private static final int TASKS = 15;
	
	private static List<Cloudlet> cloudletList;
	private static List<Vm> virtualMachines;
	
	@SuppressWarnings("unused")
	public static void main(String [] args) {
		Log.printLine("Arrancando Apartado 3...");
		
		try {

			// Paso 1 -> Inicialziar el entrono CloudSim
			CloudSim.init(USERS, Calendar.getInstance(), false);
			
			// Paso 2 -> crear el Centro de Datos
			Datacenter dataCenter = createDatacenter("Datacenter_ap3");
			
			// Paso 3 -> crearmos los Brokers
			DatacenterBroker [] brokers = new DatacenterBroker[USERS];
			int [] idBrokers = new int [USERS];
			for(int i = 0;i < USERS;i++) {
				int id = i;
				brokers[id] = createBroker();
				idBrokers[id] = brokers[id].getId();
			}
	
			// Paso 4 -> Creación de maquinas virtuales
			for(int i = 0;i < USERS; i++) {
				int id = i;
				virtualMachines = new ArrayList<Vm>();
				createVirtualMachines(VM,600,4096,idBrokers[id], 1, 1000, 20000,"Xen");
				brokers[id].submitVmList(virtualMachines);
			}
			
			// Paso 5 -> Creación del Cloudlet
			cloudletList = new ArrayList<Cloudlet>();
			for(int i = 0; i < USERS; i++) {
				int id = i;
				createCloudlets(TASKS,45000, 1, 2, 1, new UtilizationModelFull(),idBrokers[id]);
				brokers[id].submitCloudletList(cloudletList);
			}
		
			// Paso 6 -> Comenzamos la simulación
			CloudSim.startSimulation();
			
			CloudSim.stopSimulation();
			
		// Paso 7 -> Por último mostramos los resultados cuando ha acabado la simulación
			//List<Cloudlet> list = broker.getCloudletReceivedList();
			
			for(int i = 0; i < USERS;i++) {
				int id = i;
				List<Cloudlet> list = brokers[id].getCloudletReceivedList();
				printCloudletList(list);
			}
			
		
		// Si hemos llegado hasta aquí, es que todo ha salido bien
			Log.printLine("Apartado 3 finalizado!");			
		
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Han ocurrido errores inesperados");			
		}
		
		
		
	}
	
	

	private static void createVirtualMachines(int vms, int mips, int ram, int brokerId, int cores, int bw, int almacenamiento, String vmm) {
		for(int i = 0; i < vms; i++)  {
			int id = i;
			virtualMachines.add(new Vm(id,brokerId,mips,cores,ram,bw,almacenamiento,vmm,new CloudletSchedulerTimeShared()));
		}	
	}
	
	
	/**
	 * 
	 * @param clds -> número total de cloudlet
	 * @param instructions -> número de instrucciones
	 * @param fileSize -> tamaño de fichero
	 * @param outputSize -> tamaño salida
	 * @param ppt -> número de procesadores por tarea
	 * @param utilizationModel
	 * @param brokerId
	 */
	private static void createCloudlets(int clds, long instructions, long fileSize, long outputSize, int ppt, UtilizationModel utilizationModel, int brokerId) {
		for(int i = 0 ; i < clds; i++) {
			int id = i; 
			Cloudlet cloudlet = new Cloudlet(id,instructions,ppt,fileSize,outputSize,utilizationModel,utilizationModel, utilizationModel);
			cloudlet.setUserId(brokerId);
			boolean add = cloudletList.add(cloudlet);
			if(!add)
				break;
		}
	}
	
	
	private static Datacenter createDatacenter(String name) {
			
			/*Host*/
			List<Pe> coresHost = new ArrayList<Pe>();
			
			// especificaciones del host
			int cores = 4;
			int mips = 1200;
			for(int i = 0; i < cores; i++) {
				int id = i;
				coresHost.add(new Pe(id, new PeProvisionerSimple(mips)));
			}
			//coresHost.add(new Pe(0, new PeProvisionerSimple(mips)));
			//coresHost.add(new Pe(1, new PeProvisionerSimple(mips)));
			
			//int idHost = 0;
			int ram = 24576; // 24GB
			long almacenamiento = 2000000; // 2TB
			int anchoBanda = 10000; //10Gbps
			
			final int HOST = 5;
			Host [] host = new Host[HOST];
			List<Host> hosts = new ArrayList<Host>();
			/**
			hosts.add(new Host(idHost,new RamProvisionerSimple(ram), new BwProvisionerSimple(anchoBanda),
					almacenamiento, coresHost, new VmSchedulerTimeShared(coresHost)));
			**/
			
			for(int i = 0; i < HOST; i++) {
				int id = i;
				host[id] = new Host(id,new RamProvisionerSimple(ram), new BwProvisionerSimple(anchoBanda),
						almacenamiento, coresHost, new VmSchedulerTimeShared(coresHost));
				hosts.add(host[id]);
			}
			
			// especificaciones del centro de datos
			/*
			 * Arquitectura
			 * OS
			 * VMM
			 */
			String [] especificaciones = {"x86", "Linux", "Xen"};
			/*
			 * zona horaria
			 * coste por segundo
			 * coste por usuo de memoria
			 * coste por almacenamiento
			 * coste por ancho de banda
			 */
			double [] costes_zonaHoraria = {2.0,0.01,0.005,0.003,0.005}; 
			LinkedList<Storage> storageList = new LinkedList<Storage>();
			
			DatacenterCharacteristics caracteristicas = new DatacenterCharacteristics(
					especificaciones[0],especificaciones[1],especificaciones[2],hosts,costes_zonaHoraria[0],
					costes_zonaHoraria[1],costes_zonaHoraria[2],costes_zonaHoraria[3], costes_zonaHoraria[4]);
			
			Datacenter datacenter = null;
			try {
				datacenter = new Datacenter(name, caracteristicas, new VmAllocationPolicySimple(hosts),storageList,0);
			} catch(Exception e) {
				e.printStackTrace();
			}
			return datacenter;
			
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
