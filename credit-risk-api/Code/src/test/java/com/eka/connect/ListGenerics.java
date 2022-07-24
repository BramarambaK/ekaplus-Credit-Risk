package com.eka.connect;

import java.math.BigDecimal;
import java.util.UUID;

public class ListGenerics { 
	
	public static void main(String[] args) {
		 UUID fromString = UUID.fromString("5539617b-5075-4482-8bcc-26f76849eb89");
		System.out.println(fromString.toString());
		BigDecimal bd = new BigDecimal(4);
		bd.add(BigDecimal.ONE);
		System.out.println(bd.toString());
	}
}

interface Animal {

}

class Dog implements Animal {

}

class Cat implements Animal {

}
