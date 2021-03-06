package org.openmrs.module.messaging.web.dwr;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.messaging.MessagingAddressService;
import org.openmrs.module.messaging.MessagingService;
import org.openmrs.module.messaging.domain.MessagingAddress;
import org.openmrs.module.messaging.domain.gateway.Protocol;
import org.openmrs.module.messaging.domain.gateway.exception.AddressFormattingException;

public class DWRMessagingAddressService {

	Log log = LogFactory.getLog(getClass());
	
	private MessagingAddressService addressService;
	
	public DWRMessagingAddressService(){
		addressService = Context.getService(MessagingAddressService.class);
	}

	/**
	 * Returns all messaging addresses for the person ID supplied.
	 * @param personId The numeric database ID of the person in question
	 * @return
	 */
	public List<MessagingAddress> getAllAddressesForPersonId(Integer personId){
		return addressService.getMessagingAddressesForPerson(Context.getPersonService().getPerson(personId));
	}
	
	/**
	 * Returns only the 'public' messaging addresses for the person ID supplied.
	 * @param personId The numeric database ID of the person in question
	 * @return
	 */
	public List<MessagingAddress> getPublicAddressesForPersonId(Integer personId){
		return addressService.getPublicAddressesForPerson(Context.getPersonService().getPerson(personId));
	}
	
	/**
	 * Returns all messaging addresses for the currently authenticated user.
	 */
	public List<MessagingAddress> getAllAddressesForCurrentUser(){
		return addressService.getMessagingAddressesForPerson(Context.getAuthenticatedUser().getPerson());
	}
	
	/**
	 * Returns only public messaging addresses for the currently authenticated user.
	 */
	public List<MessagingAddress> getPublicAddressesForCurrentUser(){
		return addressService.getPublicAddressesForPerson(Context.getAuthenticatedUser().getPerson());
	}
	
	/**
	 * Deletes an address
	 * @param id The id of the address to delete
	 */
	public void deleteAddress(Integer id){
		MessagingAddress ma = Context.getService(MessagingAddressService.class).getMessagingAddress(id);
		Context.getService(MessagingAddressService.class).deleteMessagingAddress(ma);
	}
	
	/**
	 * Validates and saves a messaging address.
	 * @param address The address to save or update
	 * @param personId The if of the person that this address is for (can be null if you aren't creating a new address) 
	 */
	public void saveOrUpdateAddress(MessagingAddress address, Integer personId){
		Protocol protocol = Context.getService(MessagingService.class).getProtocolById(address.getProtocolId());
		MessagingAddress ma = null;
		if(address.getId() == -1){
			try {
				ma = protocol.createAddress(address.getAddress(), Context.getPersonService().getPerson(personId));
				ma.setDateCreated(new Date());
				ma.setCreator(Context.getAuthenticatedUser());
			} catch (AddressFormattingException e) {
				log.error("Error creating address in DWR", e);
			}
		}else{ //otherwise, we're modifying a previously existing address
			ma = Context.getService(MessagingAddressService.class).getMessagingAddress(address.getId());
			ma.setAddress(address.getAddress());
			ma.setProtocolId(address.getProtocolId());
			ma.setDateChanged(new Date());
			ma.setChangedBy(Context.getAuthenticatedUser());
		}
		ma.setPreferred(address.getPreferred());
		ma.setFindable(address.isFindable());
		Context.getService(MessagingAddressService.class).saveMessagingAddress(ma);
	}
	
	/**
	 * Saves an address and associates it with the currently authenticated user
	 * @param address
	 */
	public void saveOrUpdateAddressForCurrentUser(MessagingAddress address){
		saveOrUpdateAddress(address,Context.getAuthenticatedUser().getPerson().getId());
	}
}
