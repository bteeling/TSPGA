public interface QueueInterface {

	public void enqueue(Object newEntry);	//add new entry to back of queue

	public Object dequeue();				//remove and return item at front of queue
	
	public Object getFront();				//retrieve item at front of queue
	
	public boolean isEmpty();				//determine whether queue is empty
	
	public void clear();					//remove all entries from the queue
}
