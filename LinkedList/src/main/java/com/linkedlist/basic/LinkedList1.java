package com.linkedlist.basic;

//insert-> beginning, end (add elements), specific position
public class LinkedList1 {

    Node head;                   //represents first node of linkedlist

    public void insertAtBeginning(int data) {
        Node node = new Node(data);
        node.next = head;        //pointing new node to old head which may or may not be null
        head = node;             //new head
    }

    public void insertAtEnd(int data) {
        Node node = new Node(data);
        if (head == null) {
            head = node;
            return;
        }
        Node temp = head;
        while (temp.next != null) {
            temp = temp.next;
        }
        temp.next = node;
    }

    public void insertAtSpecificPosition(int data, int position) {
        Node node = new Node(data);

        if(position == 0) {       //first element: below logic will not handle this
            node.next = head;
            head = node;
            return;
        }

        Node temp = head;
        for (int i = 0; i < position - 1 && temp!=null; i++) {
            temp = temp.next;
        }

        if(temp==null) {
            System.out.println("position out of range");
            return;
        }

        node.next = temp.next;
        temp.next = node;
    }

    public void display() {
        Node temp = head;
        while (temp.next != null) {
            System.out.println(temp.data);
            temp = temp.next;
        }
        System.out.println(temp.data);
    }
}
