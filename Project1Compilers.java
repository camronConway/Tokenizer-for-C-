/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project1compilers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Camron
 */
public class Project1Compilers {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        execute(args[0]);
        
    }
    
    public static void execute(String fileName){
        int commentLevel = 0;
        ArrayList record = new ArrayList();
        readFile(fileName, record);
        record.trimToSize();

        Tokenizer tokenizer = new Tokenizer(record);
        
    }
    
    private static void readFile(String filename, ArrayList records){
        String input;//string to take input of text
        
        
        try (BufferedReader reader = new BufferedReader( new FileReader(filename))){
            int i = 0;
            while((input = reader.readLine()) != null){
                records.add(input);
                i++;
            }
        }
        catch(FileNotFoundException fnfe) {//File not found exeption
            System.out.println("Error. File not found");
            
        }
        catch(IOException ioe){//IO Error
            System.out.println("IO error");
            
        }
        catch(Exception exe){//General case
            System.out.println("Error, exception found");
            
        }
        
        
    }
    
    public static void printRecord(String record){
        char n;
        for(int i = 0; i < record.length(); i++){
            n = record.charAt(i);
            System.out.println(n);
        }
    }

}

class Tokenizer{
    public int index;
    public int peak;
    public int commentLevel;
    public String record;
    //change this to an array list eventually
    public TokenTable myTokenTable = new TokenTable();

    public Tokenizer(ArrayList Records){
        //initilize to zero
        this.commentLevel = 0;
        
        //go through file
        for(int i = 0; i < Records.size(); i++){
            this.index = 0;
            this.record = (String)Records.get(i);
            System.out.println("INPUT: " + record);
            StartState();
        }
        
    }
    
    public void StartState(){
        
        while(index < record.length()){
            //create token
            Token token = new Token();
            
            //cast as int
            int value = (int)record.charAt(index);
            
            //if in middle of muli-line comment
            if(commentLevel != 0){
                tokenizeBlockComments(record, token);
            }
            //if a character
            else if((value > 96) && (value < 122)){
                token.assign("CHAR");
                token.add(record.charAt(index));
                index++;
                tokenizeCharacter(record, token);
            }
            //if a /
            else if(value == 47){
                token.assign("OP");

                //peak ahead
                peak = peak();
                if(peak == -1){
                    token.add(record.charAt(index));
                    token.printToken();
                    myTokenTable.add(token);
                    index++;
                }
                //else another / -- line comment
                else if(peak == 47){

                    index = index +2;
                    tokenizeLineComments(record, token);
                }
                //else followed by *
                else if(peak == 42){

                    index = index+2;
                    this.commentLevel++;
                    tokenizeBlockComments(record, token);
                }
                else{
                    token.add(record.charAt(index));
                    token.printToken();
                    myTokenTable.add(token);
                    index++;
                }
            }
            //else is a *-+,
            else if(value > 41 && value < 46){
                token.assign("OP");
                token.add(record.charAt(index));
                token.printToken();
                myTokenTable.add(token);
                index++;
            }
            //else is a =
            else if(value == 61){
                //is operator
                token.assign("OP");
                token.add(record.charAt(index));
                //peak ahead
                peak = peak();
                //if ==
                if(peak == 61){
                    token.add(record.charAt(index+1));
                    index = index+2;
                    token.printToken();
                    myTokenTable.add(token);
                    
                }
                else{
                    token.printToken();
                    myTokenTable.add(token);
                    index++;
                }
                
            }
            //else is a !
            else if(value == 33){
                peak = peak();
                //if !=
                if(peak == 61){
                    token.assign("OP");
                    token.add(record.charAt(index));
                    token.add(record.charAt(index+1));
                    token.printToken();
                    myTokenTable.add(token);
                    index = index +2;
                }
                else{
                    System.out.println("Error: " + record.charAt(index));
                    index++;
                }
            }
            //else is a < or >
            else if(value == 60 || value == 62){
                
                token.assign("OP");
                token.add(record.charAt(index));
                
                peak = peak();
                //if followed by =
                if(peak == 61){
                    token.add(record.charAt(index+1));
                    token.printToken();
                    myTokenTable.add(token);
                    index = index + 2;
                }
                else{
                    token.printToken();
                    myTokenTable.add(token);
                    index++;
                }
            }
            //value is a decimal or float
            else if(value > 47 && value < 58){
                token.assign("NUM");
                numberTokenizer(record, token);
            }
            //else is a delimiter
            else if(value == 40 || value == 41 || value == 91 || value == 93
                    || value == 123 || value == 125){
                token.assign("DEL");
                token.add(record.charAt(index));
                token.printToken();
                myTokenTable.add(token);
                index++;
            }
            //else is a ;
            else if(value == 59){
                token.assign("END");
                token.add(record.charAt(index));
                token.printToken();
                myTokenTable.add(token);
                index++;
            }
            //else is a space
            else if(value == 32){
                //ignore space
                index++;
            }
            //else is something we ignore
            else{
                System.out.println("Error: " + record.charAt(index));
                index++;
            }
        }
    }
    
    public int peak(){
        if(index == record.length()-1){
            return -1;
        }
        else{
         int temp = (int)record.charAt(index+1);
         return temp;
        }
    }
    
    public void numberTokenizer(String line, Token token){
        token.add(line.charAt(index));
        peak = peak();
        if(peak > 47 && peak < 58){
            index++;
            numberTokenizer(line, token);
        }
        //if peak is a .
        else if(peak == 46){
            token.assign("FLOAT");
            token.add(line.charAt(index+1));
            index = index +2;
            floatTokenizer(line, token);
        }
        //else if peak is a E
        else if(peak == 69){
            token.add(line.charAt(index+1));
            index = index +2;
            scientificNotation(record, token);
        }
        else{
            token.printToken();
            myTokenTable.add(token);
            index++;
        }
    }
    
    public void scientificNotation(String line, Token token){
        if(index < line.length()){
            int value = (int)line.charAt(index);
            //E can be followed be a +,- or NUM
            if(value == 43 || value == 45 || (value > 47 && value < 58)){
                token.add(line.charAt(index));

                peak = peak();
                //if peak is another number
                if(peak > 47 && peak < 58){
                    index++;
                    numberTokenizer(line, token);
                }
                //if E is not followed by a number, then ERROR
                //if Format is not of ENUM or E(+/-)
                else{
                    index++;                    
                    System.out.println("Error: " + token.getName());   
                }
            }
            else{
                index++;
                System.out.println("Error: " + token.getName());
            }
        }
        
    }
    
    public void floatTokenizer(String line, Token token){
        if(index < line.length()){
            int value = (int)line.charAt(index);

            if(value > 47 && value < 58){
                token.add(line.charAt(index));
                index++;
                floatTokenizer(line, token);
            }
            else if(value == 69){
                token.add(line.charAt(index));
                index++;
                scientificNotation(line, token);
            }
            else{
                token.printToken();
                myTokenTable.add(token);
            }
        }
        else{
            token.printToken();
            myTokenTable.add(token);
        }
        
        
    }
    
    public void tokenizeLineComments(String line, Token token){
        //flush the line
        while(index < line.length()){
            index++;
        }  
    }
    
    public void tokenizeBlockComments(String line, Token token){

        
        while(index < line.length() && commentLevel != 0){
            
            int value = (int)line.charAt(index);
            
            //if closing
            if(value == 42){
                peak = peak();
                if(peak == 47){
                    commentLevel--;
                    //index++;
                    index = index +2;
                }
                else{
                    index++;
                }
            }
            //if another level of comment
            else if(value == 47){
                peak = peak();
                if(peak == 42){
                    commentLevel++;
                    //index++;
                    index = index + 2;
                }
                else{
                    index++;
                }
            }
            else{
                index++;
            }
        }
        
    }
    
    public void tokenizeCharacter(String line, Token token){
        
        if(index < line.length()){
            int value = (int)line.charAt(index);


            //if is a character value
            if((value > 96) && (value < 122)){
                //append to token
                token.add(line.charAt(index));
                index++;
                //call method again
                tokenizeCharacter(line, token);
            }
            else{
                myTokenTable.add(token);
                token.printToken();
            }  
        }
        //this occurs when token is at the end of the line
        else{
            myTokenTable.add(token);
            token.printToken();
        }
    }
}

class Token{
    private String name;
    private String type;
    private Token next;
    
    public Token(){
        this.name = "";
        this.type = "";
    }
    
    public void add(char a){
        this.name = name+a;
    }
    
    public void assign(String type){
        this.type = type;
    }
    
    public void printToken(){
        System.out.println(type + ": " + name);
    }
    
    public String getName(){
        return this.name;
    }
    
    public String getType(){
        return this.type;
    }
}


class TokenTable{
    
    //public Token[] tokenTable;
    public ArrayList tokenTable;
    public int index;
    public int value;
    
    public TokenTable(){
        tokenTable = new ArrayList();
        index = 0;
        value = 0;
    }
    
    public void add(Token token){
       // tokenTable[index] = token;
       tokenTable.add(index, token);
        reassign(index);
        index++;
        value++;
    }
    
    public void reassign(int index){
        //String test = tokenTable[index].getType();
        Token tester = (Token)tokenTable.get(index);
        String test = tester.getType();
        if(test.equals("CHAR")){
            String key = tester.getName();
            switch(key){
                case "else":
                case "if":
                case "int":
                case "return":
                case "void":
                case "while":
                    //tokenTable[index].assign("KEY");
                    tester.assign("KEY");
                    break;
                default:
                    //tokenTable[index].assign("ID");
                    tester.assign("ID");
            }
                
        }
    }
      
}

