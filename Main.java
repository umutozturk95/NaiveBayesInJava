/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Umut Ozturk
 */

import java.util.*;
public class Main {
   public static void main(String args[]){
     
         if(args.length!=3){
           
           System.out.println("Invalid argument!");
           return;
       }
       
       WSDNaiveBayes wsd=new WSDNaiveBayes();
       wsd.startWSD(args[0],args[1],args[2]);
       
   }
}
