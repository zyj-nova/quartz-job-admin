package com.bjtu.zyj.jobadmin2;

import java.util.concurrent.LinkedBlockingQueue;

public class Test {

    // 阻塞队列
    static LinkedBlockingQueue<Student> queue = new LinkedBlockingQueue();

    static class Student{

        private String name;
        private Subject subject;

        public Student(String name, Subject subject){
            this.name = name;
            this.subject = subject;
        }
    }

    static class Subject{
        String name;
        double grade;

        public Subject(String name, double grade){
            this.name = name;
            this.grade = grade;
        }
    }
    // 生产者
    static class Producer implements Runnable{

        private LinkedBlockingQueue queue;

        public Producer(LinkedBlockingQueue queue){
            this.queue = queue;
        }

        @Override
        public void run(){

        }

        public void addStudent(Student student) throws InterruptedException {
            synchronized (queue){
                queue.put(student);
                queue.notify();
                Thread.sleep(500);
            }
        }
    }

    // 消费者
    static class Consumer implements Runnable{
        private final LinkedBlockingQueue queue;

        public Consumer(LinkedBlockingQueue queue){
            this.queue = queue;
        }

        @Override
        public void run(){
            try {
                Student s = pollStudent();
                // 及时释放锁，避免处理逻辑占用锁资源
                // 计算分数
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public Student pollStudent() throws InterruptedException {
            Student ret = null;
            synchronized (queue){
                while (queue.size() == 0){
                    queue.wait();
                }
                ret = (Student) queue.poll();
            }
            return ret;
        }
    }

    public static void main(String[] args) {
        Producer p1 = new Producer(queue);
        Producer p2 = new Producer(queue);
        Producer p3 = new Producer(queue);

        Consumer s1 = new Consumer(queue);
        Consumer s2 = new Consumer(queue);
        Consumer s3 = new Consumer(queue);



    }
}
