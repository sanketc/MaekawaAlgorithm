package code.client.util;

import java.util.Comparator;

import code.message.maekawa.RequestMessage;

/**
 * Class for comparing/ordering two or more requests.
 * 
 * @author Sanket Chandorkar
 */
public class RequestComparator implements Comparator<RequestMessage> {

	@Override
	public int compare(RequestMessage o2, RequestMessage o1) {
		if (o1.getClock().getTimeStamp() < o2.getClock().getTimeStamp()) {
			return 1; /* o1 >priority> o2 */
		} else if (o1.getClock().getTimeStamp() == o2.getClock().getTimeStamp()) {
			/* Compare process Id: lower less means high priority */
			int id1 = Integer.parseInt(o1.getConfig().getId());
			int id2 = Integer.parseInt(o2.getConfig().getId());
			if (id1 <= id2) 
				return 1;
			return -1;
		} else {
			return -1; /* o1 <priority< o2 */
		}
	}
}