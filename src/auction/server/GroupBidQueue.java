package auction.server;

public class GroupBidQueue{

	private Node head = null;
	private Node tail = null;

	private class Node{
		private GroupBid elem;
		private Node next;

		public Node(GroupBid elem){
			this.elem = elem;
			this.next = null;
		}
	}

	public void enqueue(GroupBid elem){
		Node n = new Node(elem);
		if( head == null ){
			head = n;
		}else{
			tail.next = n;

		}
		tail = n;
	}

	public GroupBid dequeue(){
		GroupBid elem = head.elem;
		head = head.next;
		return elem;
	}
}