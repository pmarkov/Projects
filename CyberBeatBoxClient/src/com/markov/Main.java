package com.markov;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your user name: ");
        new BeatBoxClient().startUp(scanner.nextLine());
    }
}
