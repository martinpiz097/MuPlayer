package org.muplayer.test;

import java.util.Arrays;

public class ReflectionTest {
    public static void main(String[] args) {
        Persona p = new Persona(1);
        Class<? extends Persona> clazz = p.getClass();
        Class<?>[] classes = clazz.getClasses();
        Object[] signers = clazz.getSigners();
        Class<?>[] declaredClasses = clazz.getDeclaredClasses();

        System.out.println(Arrays.toString(classes));
        System.out.println("------------------------");
        System.out.println(Arrays.toString(signers));
        System.out.println("------------------------");
        System.out.println(Arrays.toString(declaredClasses));
        clazz.asSubclass(Personita.class);


    }

}
