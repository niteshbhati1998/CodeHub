package com.linkedlist.basic;

public class LinkedList {

    Node head;                   //points to first node of linkedlist

    //head
    //2    3    4    5
    public void insertAtBeginning(int data) {
        Node node = new Node(data);
        node.next = head;        //pointing new node to old head
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

        if(position == 0) {
            node.next = head;
            head = node;
            return;
        }

        Node temp = head;
        for (int i = 0; i < position - 1; i++) {
            temp = temp.next;
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

    public static void main(String[] args) {
        LinkedList1 linkedList = new LinkedList1();
        linkedList.insertAtBeginning(10);
        linkedList.insertAtEnd(20);
        linkedList.insertAtEnd(30);
        linkedList.insertAtEnd(40);
        linkedList.insertAtSpecificPosition(100, 0);
        linkedList.display();
    }
}
