package myproject;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.io.*;
import java.lang.NullPointerException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Orquillas{
    public static void main(String args[]) {
        Terminal t = new Terminal("mp4.in");
        t.execute();
    }   
}

abstract class Node{
    public int type;
    public String name;
    public DIR parent;
    protected ArrayList<Node> children;
    protected String content;
    public boolean equality(Node x){
        if(this.type==x.type && this.name.equals(x.name))
            return true;
        return false;
    }
    public String getType(){ return this.type == 1 ? "FILE": "DIRECTORY";}
    public abstract Node clone();
    public abstract ArrayList<Node> getMyChildren();
    public abstract boolean makeChild(Node n);
    public abstract boolean removeChild(Node n);
    public abstract Node getMyChild(int i);  
}

class DIR extends Node{
    public ArrayList<Node> children;
    public DIR(String DIRName){
        this.name = DIRName;
        this.type = 0;
        this.parent = null;
        children = new ArrayList<Node>();
    }

    public DIR clone(){
      DIR cp = new DIR(this.name);
      cp.parent = this.parent;
      cp.type = this.type;
      cp.children = (ArrayList<Node>) this.children.clone();
    return cp;
  }
    public ArrayList<Node> getMyChildren(){ return this.children; }
    public Node getMyChild(int i){ return this.children.get(i); }
    public boolean makeChild(Node n){return this.children.add(n);}
    public boolean removeChild(Node n){
      for(int i = 0, m = children.size(); i < m; i++)
         if(children.get(i).equality(n)){children.remove(i);return true;}
             return false;}
}

class FILE extends Node{
    public String content;
    public FILE(String filename){
        this.name = filename;
        this.type = 1;
        this.parent = null;
        this.content = "";
    }
    public FILE clone(){
    FILE cp = new FILE(this.name);
    cp.parent = this.parent;
    cp.type = this.type;
    cp.content = this.content;
    return cp;
  }
    public String getContent(){ return this.content; }
    public void setContent(String s){ this.content = s; }
    public ArrayList<Node> getMyChildren() { throw new UnsupportedOperationException("Method seeChildren not overriden.");}
    public Node getMyChild(int i) { throw new UnsupportedOperationException("Method seeChildren not overriden.");}
    public boolean makeChild(Node n) { throw new UnsupportedOperationException("Method addChild not overriden.");}
    public boolean removeChild(Node n) {   
        for(int i = 0, m = this.parent.children.size(); i < m; i++)
         if(children.get(i).equality(n)){children.remove(i);return true;}
             return false;
    }
}

class FileSystem{
    private Node root;
    private Node curr;
    public FileSystem(String save){
        if(!new File(save).exists()){
            root = new DIR("root");
            curr = root;
        }
        else{
            try {
                Scanner scan = new Scanner(new File(save));
                root = new DIR(scan.nextLine());
                Queue<DIR> parents = new LinkedList<DIR>();
                parents.add(((DIR)root));
                while(scan.hasNext()){
                    int k=0;
                    int prevpt = 0; 
                    Node newNode = null;
                    if(scan.hasNextInt()) k = scan.nextInt();
                    for(int i = 0; i<k; i++){
                        int myType=0, myParent =0, childrenNum =0 ;
                        if(scan.hasNextInt()) myType =scan.nextInt();
                        if(scan.hasNextInt()) myParent= scan.nextInt();
                        if(scan.hasNextInt()) childrenNum= scan.nextInt();
                        for(int pt1 = 0; prevpt < myParent; pt1++) parents.remove();
                        if(myType==0){
                            newNode = new DIR(scan.next());  
                            if(childrenNum > 0) parents.add((DIR) newNode);                                
                        }
                        else if(myType == 1) newNode = new FILE(scan.next());                                 
                        newNode.parent = parents.element();
                        parents.element().children.add(newNode);                                               
                    }
                    parents.remove();
                    curr = root;
                }
            } catch (FileNotFoundException ex){
                
           }              
        }
    }
    public void FSclose() throws IOException{
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("save.txt"), "utf-8"));  // filename
        Queue<DIR> tmp = new LinkedList<DIR>();
        int LevelNumber = ((DIR)root).children.size();
        int currParent, pilaKaAnak = 0, ct = 0;
        writer.write(root.name + " \n");    // content
        tmp.add((DIR) root);
        while(!(tmp.isEmpty())){
            currParent = 0;
            pilaKaAnak = 0;
            writer.write(LevelNumber + " ");
            String child ="";
            ct = 0;
            for(int i = 0; i<LevelNumber; i++){
                Node tmpcurr = tmp.element().getMyChild(ct);
                int tmp1 = 0;
                if(tmpcurr.type==0){
                    if(((DIR)tmpcurr).getMyChildren().size()> 0){
                        tmp.add((DIR)tmpcurr);
                        pilaKaAnak+=((DIR)tmpcurr).getMyChildren().size();
                    } 
                    tmp1 = ((DIR)tmpcurr).getMyChildren().size();
                    child = child + tmpcurr.type + " "+ currParent + " " + tmp1 + " "+ tmpcurr.name + " ";
                }
                else{
                    child = child + tmpcurr.type + " "+ currParent + " " + tmpcurr.name + " ";
                   String buffer = ((FILE)tmpcurr).content;
                   Path newFile = Paths.get(((FILE)tmpcurr).name);
                   Files.write(newFile, buffer.getBytes(), StandardOpenOption.CREATE);
                   Files.write(newFile, buffer.getBytes(), StandardOpenOption.WRITE);
                }
                ct++;
                if(ct==tmp.element().getMyChildren().size()){ ct=0; currParent++; tmp.remove();}
            }
            child = child + '\n';
            LevelNumber = pilaKaAnak;
            writer.write(child);
       }
        writer.close();
 }
    
    private Node start(String s){
       if(s.charAt(0) == '/') 
           return root;
       else  
           return curr;
    }
    private String[] path(String str){
    if(str.charAt(0)=='/') 
        return Arrays.copyOfRange(str.split("/"),1,str.split("/").length);
     else return str.split("/");
    }
    private Node seek( Node begin, String[] str){ 
    Node n = begin;   
    for(int i = 0 ; i < str.length; i++){
        if(str[i].equals(".")) continue; 
        else if(n!=root &&str[i].equals("..")) n = n.parent;
        else{
           Node c = new DIR(str[i]);// n2 = n;
           int m = ((DIR)n).getMyChildren().size();
            for(int j = 0 ; j < m; j++)
                if(((DIR)n).getMyChildren().get(j).equality(c)){ n = ((DIR)n).getMyChildren().get(j);break;}
      }
    }
    return n;
  }
    public void cd(String adto){
         if(adto.equals("")) curr = root;
         else{Node tmp = seek(start(adto),path(adto));
         if(tmp!=null) curr = tmp;
         }
  }
   
    public Node getCurrNode(){return curr;}
    public String getCurr(){
    String s = "";
    for(Node tmp = curr; tmp!=null; s="/"+tmp.name +s,tmp = tmp.parent);
    return s;
    }
    public boolean add(Node nodeToadd, String placeToadd){
    String[] inst = path(placeToadd);
    if(this.root == null){
        this.root = new DIR(inst[inst.length-1]); 
        this.curr = this.root; 
        return true; 
    }
    Node m = seek(start(placeToadd),inst);
    if(m==null){
        return false;
    }
    else if (!m.name.equals(inst[inst.length-1])) nodeToadd.name = inst[inst.length-1];
    nodeToadd.parent = ((DIR)m);
    return ((DIR)m).makeChild(nodeToadd);
  }

    public boolean add(String dirToplace,int type){
    if(type == 0) return this.add(new DIR(""),dirToplace);
    return this.add(new FILE(""),dirToplace);
  }
     public boolean addNode(String str, int type){
        Node newNode = null;
        String path[] = str.split("/");
        String name = path[path.length - 1];
        String toWhere = "";
        
        
        for(int i = 0; i < path.length - 1; i++){
            toWhere = toWhere + path[i];
            if (i < path.length - 2)
                toWhere = toWhere + "/";
        }
        if(type == 0){
            newNode = new DIR(name);
            
        }
        else{
            try {
                newNode = new FILE(name);
                Writer newFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name), "utf-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FileSystem.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        //myOutput.write(toWhere);
        if(forShow(toWhere)!=null){
            newNode.parent = (DIR)forShow(toWhere);
            return ((DIR)forShow(toWhere)).children.add(newNode);  
        }
        else
            return false;
    }
     
      public boolean remove(String str, int type){
       
        if(forShow(str)!=null){
            Node tmp = forShow(str);
            DIR prev = tmp.parent;
        System.out.println(tmp.parent.name);
        return prev.children.remove(tmp);
        }
          
        return false;
 
    }
     
     
    public Node get(String name){
     return seek(start(name),path(name));
   }
    public String[] search(String name){  // search file or directory starting at curr.
      ArrayList<String> found = new ArrayList<String>();
      int flag = 0;
      Queue<Node> visit = new LinkedList<Node>();  // we use breadth-first search.
      visit.add(root);
      for(Node tmp = root; visit.peek()!=null;){
        for(int i = 0,n = tmp.getMyChildren().size(); i < n; i++){
          if(tmp.getMyChild(i).type == 0) visit.add(tmp.getMyChild(i));
          if(tmp.getMyChild(i).name.equals(name)){  // if name matches get path string.
            String src_path = "/"+ name;
            for(Node tmp2 = tmp; tmp2!=null; tmp2=tmp2.parent)
              src_path = "/"+tmp2.name +src_path;
            found.add(src_path);
          }
        }
        visit.remove(); 
        tmp = visit.peek();
        flag = 1;
      }
     if(flag==1)
      return found.toArray(new String[found.size()]);  // return absolute path of every instance of the item being searched.
     else 
         return null;
    }
    
    public boolean validName(String fileName){

        String regex = "^*/:?\\<>|\"";
        int size = regex.length();
        for(int i=0; i<size;i++){
            if(fileName.contains(regex.substring(i,i+1)))
                return false;
        }
        return true;
      }
    
    public Node forShow(String argument){
        Node tmp;
        String path[] = argument.split("/");
        int num = path.length;
        int i;
      
        if(argument.length() == 0){
            return curr;
        }
        if(argument.charAt(0) == '/'){
            tmp = root;
            i = 2;
        }
        else if(path[0].equals("..")){
            tmp = curr.parent;
            i = 1;
        }
        else{
            tmp = curr;
            i = 0;
        }
        
        for (; i < num; i++){
            int flag = 0;
            if (path[i].equals("..")){
                tmp = tmp.parent;
            }
            
            else{
                for(int j=0; j<((DIR)tmp).children.size(); j++){
                    if(path[i].equals(((DIR)tmp).children.get(j).name)){
                        tmp = ((DIR)tmp).children.get(j);
                        flag=1;
                        break;
                    }
                }  
                if(flag == 0)
                    return null;
            }
        }
        return tmp;
    }
 }

class Terminal{

    private File commands;
    private FileSystem fs;
    public Terminal(String in){
        commands = new File(in);
        fs = new FileSystem("save.txt");
    }
 
    public void execute(){
        try {
            Writer myOutput = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("mp4.out"), "utf-8"));
            Scanner sc = new Scanner(commands);
            while(sc.hasNextLine()){
                String[] s = sc.nextLine().split(" ");
                switch(s[0]){
                
                    case "mkdir": if(s.length>1){
                                        if(fs.validName(s[1])){
                                        if(!fs.add(s[1], 0)) 
                                             myOutput.write("File Name already exists.");
                                        } else myOutput.write("Invalid name");
                                    }  
                                   else myOutput.write("usage: mkdir <directory name>");
                                    break;
                                    
                             
                    case "rmdir": if(s.length>1){if(!fs.remove(s[1], 0)) myOutput.write("File does not exist.");} 
                                  else  myOutput.write("usage: rmdir <directory name>");
                                  break;
                    case "cd":  fs.cd(s[1]);  break;
                    case "rm": if(s.length>1){if(!fs.remove(s[1], 1)) myOutput.write("File does not exist.");} else myOutput.write("usage: rm <file name>");  break;
                    case "cp":
                      try{
                          if(fs.validName(s[2]))                       
                          fs.add(fs.get(s[1]).clone(),s[2]);
                      else 
                        myOutput.write("Invalid Name");
                      }
                      catch(ArrayIndexOutOfBoundsException e){
                      myOutput.write("usage: cp source_file/source_directory target_file/target_directory");}
                      break;
                    case "rn":   
                        if(s.length>1){
                        Node myNode = fs.get(s[1]);
                        if(myNode!=null){
                            if(fs.validName(s[2])){
                             fs.add(s[2], myNode.type); 
                                    Node copy = myNode.clone();
                                    Node x = fs.get(s[2]);
                                    copy.name =s[2]; 
                                    x = copy;   
                             }
                            else myOutput.write("Invalid file name");
                            }
                        else myOutput.write("File does not exist.");
                        }
                        else myOutput.write("usage: rn <file name>");
                        
                    case "mv":
                        Node mynewNode = fs.get(s[1]);
                        if(s.length>1){
                        if(mynewNode!=null){
                         if(fs.validName(s[2])){
                        fs.add(fs.get(s[1]).clone(),s[2]);
                        fs.remove(s[1],fs.get(s[1]).type);}
                         else myOutput.write("Invalid Name.");
                        }
                        else myOutput.write("File does not exist");
                        }
                        else myOutput.write("usage: mv source_file/source_directory target_file/target_directory");
                        break;
                      
                    case "ls": 
                        Node currentDir = fs.get(".");
                        if(s.length>1){
                              String file_type = s[1].substring(1, s[1].length());
                                for(int i=0; i<currentDir.getMyChildren().size(); i++)
                                    if(currentDir.getMyChildren().get(i).name.contains(file_type))
                                        myOutput.write(currentDir.getMyChildren().get(i).name);
                        }    
                        else
                            for(int i=0; i<currentDir.getMyChildren().size(); i++)
                                myOutput.write(currentDir.getMyChild(i).name);
                        break;
                    case "whereis": 
                        String[] str = fs.search(s[1]);
                        if(str.length==0) myOutput.write("No such file or directory exists");
                        if(s.length>1){
                          for(int i=0; i<str.length;i++)
                            myOutput.write(str[i]);
                        }
                        else
                            myOutput.write("usage: whereis <file name>");
                        break;
                    case">>":
                        if(s.length>1){
                            if(fs.validName(s[1])){
                                if(fs.forShow(s[1])== null){
                                    fs.addNode(s[1], 1);
                                }
                                FILE tmp2 = (FILE) fs.forShow(s[1]);                               
                                char decide = 'Y';
                                String content2 = tmp2.content;
                                while(decide == 'Y'){
                                    Scanner line = new Scanner(System.in);
                                    content2 = content2 + line.nextLine() + "\n";
                                    System.out.println("Enter another line Y/N");
                                    Scanner dec = new Scanner(System.in);
                                    if("Y".equals(dec.nextLine()))
                                        decide = 'Y';
                                    else
                                        decide = 'N';
                               }
                                 
                                 tmp2.content = content2;
                                 myOutput.write(tmp2.content);
                               
                            }
                            else
                                myOutput.write("Invalid Filename");
                        }
                        else
                            myOutput.write("usage: >> <file name>");
                        break;
                  
                        case ">":
                            if(s.length>1){
                            if(fs.validName(s[1])){
                                 
                                  fs.addNode(s[1], 1);
                                 char decide = 'Y';
                                 String content1 = "";
                                 while(decide == 'Y'){
                                     Scanner line = new Scanner(System.in);
                                     content1 = content1 + line.nextLine()+ "\n";
                                     System.out.println("Do you want another line? Y/N");
                                     Scanner dec = new Scanner(System.in);
                                     if("Y".equals(dec.nextLine()))
                                         decide = 'Y';
                                     else
                                         decide = 'N';
                                }
                                FILE tmp1 = (FILE) fs.forShow(s[1]);
                                tmp1.content =   content1;
                                System.out.println(tmp1.content);
                                 
                            }
                        }
                        else
                            System.out.println("usage: > <file name>");
                        break;
                         
                        
                    case "edit":
                       if(s.length>1){
                            if(fs.validName(s[1])){
                                if(fs.forShow(s[1])!=null){  
                                    FILE tmp2 = (FILE) fs.forShow(s[1]);
                                    myOutput.write(tmp2.content);
                                    char decide = 'Y';
                                    
                                    String content2 = tmp2.content;
                                    while(decide == 'Y'){
                                        Scanner line = new Scanner(System.in);
                                        content2 = content2 + line.nextLine() + "\n";
                                        System.out.println("Enter another line? Y/N");
                                        Scanner dec = new Scanner(System.in);
                                        if("Y".equals(dec.nextLine()))
                                            decide = 'Y';
                                        else
                                            decide = 'N';
                                   }
                                 tmp2.content= content2;
                                }
                                else
                                    myOutput.write("File does not exist.");
                               
                            }
                            else
                                myOutput.write("Invalid Filename");
                        }
                        else
                            myOutput.write("usage: >> <file name>");
                        break;
                        
                        
                    case "show": 
                      
                        if(s.length>1){
                            
                            if(fs.validName(s[1])){
                                Node showFile = fs.forShow(s[1]);
                                if(showFile!=null){  
                                    FILE tmp2 = (FILE) fs.forShow(s[1]);
                                    myOutput.write(tmp2.content +"\n");
                                }
                                else
                                    myOutput.write("File does not exist" +"\n");
                            }
                        }
                        else
                            myOutput.write("usage: show <file name>" +"\n");
                        break;
                    
                    default: myOutput.write("Command not found"); break;
                }          
            } 
        myOutput.close();
        fs.FSclose();
 
        }
       catch(IOException e){
      
    }
    }
}

