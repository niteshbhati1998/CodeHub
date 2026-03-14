package com.linkedlist.basic;

//move the last k node to the front
public class LinkedList3 extends LinkedList1 {

    public void moveLastNodeToFirst() {
        if (head == null || head.next == null) {
            return;
        }

        Node temp = head;
        while (temp.next.next != null) {
            temp = temp.next;
        }
        Node secondLast = temp;
        Node last = secondLast.next;
        secondLast.next = null;
        last.next = head;
        head = last;
    }

    public void moveKNodesToFirst(int k) {
        if(head==null) {
            return;
        }

        if(k==0) {
            return;
        }

        //count total nodes
        Node temp = head;
        int counter= 1;
        while(temp.next!=null) {
            temp = temp.next;
            counter++;
        }

        if(k==counter) {
            return;
        }

        int position = counter - k;          //split position
        temp = head;
        for(int i=0;i<position-1;i++) {
            temp = temp.next;
        }
        Node nextNode = temp.next;
        Node head1 = nextNode;
        temp.next = null;

        //iterate nextnode to reach to last
        while(nextNode.next!=null) {
            nextNode=nextNode.next;
        }
        nextNode.next = head;
        head = head1;
    }

    public static void main(String[] args) {
        LinkedList3 ll = new LinkedList3();
        ll.insertAtEnd(10);
        ll.insertAtEnd(20);
        ll.insertAtEnd(30);
        ll.insertAtEnd(40);
        ll.insertAtEnd(50);
        ll.insertAtEnd(60);
        ll.insertAtEnd(70);
        ll.insertAtEnd(80);
        ll.insertAtEnd(90);
        ll.display();

        //ll.moveLastNodeToFirst();
        //ll.display();

        ll.moveKNodesToFirst(9);
        ll.display();
    }
}
