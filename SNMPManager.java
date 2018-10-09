package powermonitoring.actions;
import java.io.IOException;

import org.snmp4j.*;
import org.snmp4j.smi.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class SNMPManager {
	Snmp snmp = null;
	String address = null;
	
	/***
	 * Creates new SNMP manager
	 * @param address IP address
	 */
	public SNMPManager(String address){
		this.address = address;
	}
	
	/***
	 * Start the Snmp session.	
	 * @throws IOException 
	 */
	public void start() throws IOException {		
		TransportMapping transport = new DefaultUdpTransportMapping();
		snmp = new Snmp(transport);
		snmp.listen();
		}
	
	/***
	 * Method which takes a single OID and returns the response from the agent as a String.
	 * @param oid OID
	 * @return Response from agent
	 * @throws IOException
	 */
	public String getAsString(String oid) throws IOException {
	    try {OID cOid = new OID(oid);
		ResponseEvent event = get(new OID[] { cOid });
		return event.getResponse().get(0).getVariable().toString();
		} finally {
			snmp.close();
		}
	}
	
	/***
	 * This method is capable of handling multiple OIDs
	 * @param oids 
	 * @return
	 * @throws IOException
	 */
	private ResponseEvent get(OID oids[]) throws IOException {
		PDU pdu = new PDU(); 
		for (OID oid : oids) {
			pdu.add(new VariableBinding(oid));
		}
		
		pdu.setType(PDU.GET);
		ResponseEvent event = snmp.send(pdu, getTarget(), null);
		
		if(event != null) {
			return event;
		}
	
		throw new RuntimeException("GET timed out");
	}
	
	/***
	 * This method returns a Target, which contains information about where the data should be fetched and how.
	 * @return
	 */
	private Target getTarget() {
		Address targetAddress = GenericAddress.parse(address);
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));

		//Can add more customizable data here for more control over the snmp calls.
		target.setAddress(targetAddress);
		target.setRetries(0);
		target.setTimeout(1500);
		target.setVersion(SnmpConstants.version2c);

		return target;
	}
}
