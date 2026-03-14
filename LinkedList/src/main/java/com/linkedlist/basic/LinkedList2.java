package com.linkedlist.basic;

//remove-> beginning, end, specific position
public class LinkedList2 extends LinkedList1{

    public void removeFromBeginning() {
        if (head == null) {
            return;
        }
        head = head.next;
    }

    public void removeFromEnd() {
        if (head == null) {             //no element
            return;
        }

        if (head.next == null) {        //one element
            head = null;
            return;
        }

        Node temp = head;
        while (temp.next.next != null) {  //finding second last element
            temp = temp.next;
        }
        temp.next = null;
    }

    public void removeFromSpecificPosition(int position) {

        if(position==0) {
            if(head==null) {
                return;
            }
            head=head.next;
            return;
        }

        Node temp = head;
        for(int i=0;i<position-1 && temp!=null;i++) {
            temp = temp.next;
        }
        if(temp==null) {
            System.out.println("position out of range");
            return;
        }
        Node remove = temp.next;
        temp.next = remove.next;
        remove.next=null;
    }

    public void display() {
        Node temp = head;
        while (temp.next != null) {
            System.out.println(temp.data);
            temp = temp.next;
        }
        System.out.println(temp.data);
    }

    public static void main(String[] args) {
        LinkedList2 ll = new LinkedList2();
        ll.insertAtBeginning(10);
        ll.insertAtEnd(20);
        ll.insertAtEnd(30);
        ll.insertAtEnd(40);
        //ll.insertAtSpecificPosition(100, 2);
        ll.display();

        ll.removeFromBeginning();
        ll.removeFromEnd();
        ll.removeFromSpecificPosition(1);
        ll.display();
    }
}
