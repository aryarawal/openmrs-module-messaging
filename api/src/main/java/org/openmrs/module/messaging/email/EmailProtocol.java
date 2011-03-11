package org.openmrs.module.messaging.email;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.module.messaging.domain.Message;
import org.openmrs.module.messaging.domain.MessagingAddress;
import org.openmrs.module.messaging.domain.gateway.Protocol;
import org.openmrs.module.messaging.domain.gateway.exception.AddressFormattingException;
import org.openmrs.module.messaging.domain.gateway.exception.MessageFormattingException;
import org.springframework.util.StringUtils;

/**
 * Email protocol for use with Messaging Module
 */
public class EmailProtocol extends Protocol {

	/**
	 * pattern for use with validating email addresses
	 */
	static final Pattern emailPattern = Pattern.compile(
			"^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * logging facility
	 */
	private static Log log = LogFactory.getLog(EmailProtocol.class);
	
	/**
	 * provide protocol's name
	 */
	@Override
	public String getProtocolName() {
		return "Email";
	}
	
	/**
	 * validate addresses and create a Message object
	 */
	@Override
	public Message createMessage(String messageContent, String toAddress, String fromAddress) throws MessageFormattingException, AddressFormattingException {
		MessagingAddress to=null,from=null;
		to = createAddress(toAddress,null);
		from = createAddress(fromAddress,null);
		if(!messageContentIsValid(messageContent))
			throw new MessageFormattingException("Message has no content");
		
		Message result = new Message(to, from, messageContent);
		result.setProtocol(this.getClass());
		
		log.debug("Created message from " + fromAddress + " to " + toAddress);
		
		return result;
	}
	

	@Override
	public Message createMessage(String messageContent, MessagingAddress to, MessagingAddress from) throws MessageFormattingException {
		if(!messageContentIsValid(messageContent))
			throw new MessageFormattingException("Message has no content");
		
		Message result = new Message(to, from, messageContent);
		result.setProtocol(this.getClass());
		
		log.debug("Created message from " + from.getAddress() + " to " + to.getAddress());
		
		return result;
	}

	@Override
	public Message createMessage(String messageContent, Set<MessagingAddress> to, MessagingAddress from) throws MessageFormattingException {
		if(!messageContentIsValid(messageContent))
			throw new MessageFormattingException("Message has no content");
		
		Message result = new Message(to, from, messageContent);
		result.setProtocol(this.getClass());
		StringBuilder recipientList = new StringBuilder();
		for(MessagingAddress toAdr: to){
			recipientList.append(toAdr.getAddress() + " ");
		}
		log.debug("Created message from " + from.getAddress() + " to " + recipientList.toString());
		return result;
	}

	/**
	 * validate and generate a MessagingAddress object
	 * 
	 * @param address the address to be used
	 * @param person the person to be connected with this address
	 * @return the newly created MessagingAddress
	 */
	@Override
	public MessagingAddress createAddress(String address, Person person) throws AddressFormattingException {

		// validate the address
		if (!addressIsValid(address))
			throw new AddressFormattingException("The email address you entered was not valid.");

		MessagingAddress result = new MessagingAddress(address, person);
		result.setProtocol(this.getClass());
		
		log.debug("Created address for " + person + " (" + address + ")");
		
		return result;
	}

	/**
	 * only return false if the content is null or otherwise empty
	 */
	@Override
	public boolean messageContentIsValid(String content) {
		return StringUtils.hasText(content);
	}

	/**
	 * determine if an address is valid
	 */
	@Override
	public boolean addressIsValid(String address) {
		if (!StringUtils.hasText(address))
			return false;
		Matcher matcher = emailPattern.matcher(address);
		return matcher.matches();
	}
}
