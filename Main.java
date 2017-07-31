package com.ricarvy;

import com.csvreader.CsvReader;

import javax.crypto.NullCipher;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.lang.Math;
import java.util.HashMap;


/**
 * Created by MECHREVO on 2017/7/30.
 */


/**
 * 构建Decision Tree的节点结构
 * Attributes：
 * Parent_Key:记录当前节点父节点的Key,String,默认为“”
 * Parent_Value:记录当前节点父节点的Value，String,默认为“”
 * Self_Key:记录当前节点的Key，String,默认为“”
 * if_leaves_node:记录当前节点的类别index，当且仅当为叶结点时有值,String,默认为""
 * Child_Node_List:记录当前节点下的所有子节点,ArrayList<Tree_Node>,默认长度为0
 *
 *
 * Functions:
 * PK,PV,SK,if_leaves_node，Child_Node_List的getter和setter方法
 * */
class Tree_Node{
    private String Parent_Key;
    private String Parent_Value;
    private String Self_Key;
    private String if_leaves_node;
    private ArrayList<Tree_Node> Child_Node_List;
    public Tree_Node(){
        this.if_leaves_node="";
        Child_Node_List=new ArrayList<Tree_Node>();
    }
    public Tree_Node(String PK, String PV, String SK){
        this.Parent_Key=PK;
        this.Parent_Value=PV;
        this.Self_Key=SK;
        this.if_leaves_node="";
        this.Child_Node_List=new ArrayList<Tree_Node>();
    }

    public String getParent_Key() {
        return Parent_Key;
    }

    public void setParent_Key(String parent_Key) {
        Parent_Key = parent_Key;
    }

    public String getParent_Value() {
        return Parent_Value;
    }

    public void setParent_Value(String parent_Value) {
        Parent_Value = parent_Value;
    }

    public String getSelf_Key() {
        return Self_Key;
    }

    public void setSelf_Key(String self_Key) {
        Self_Key = self_Key;
    }

    public String isIf_leaves_node() {
        return if_leaves_node;
    }

    public void setIf_leaves_node(String if_leaves_node) {
        this.if_leaves_node = if_leaves_node;
    }

    public ArrayList<Tree_Node> getChild_Node_List() {
        return Child_Node_List;
    }

    public void setChild_Node_List(ArrayList<Tree_Node> child_Node_List) {
        Child_Node_List = child_Node_List;
    }

}


public class Main {
    /**
     *从csv文件中读取数据
     *Param:
     * FilePath:要读取的CSV文件路径，String
     *
     * return:一个承载了该文件的的CSVReader
     * */
    public static CsvReader Read_data_from_csv(String FilePath){
        try {
            CsvReader csvReader = new CsvReader(FilePath, ',', Charset.forName("UTF-8"));
            return csvReader;
        }catch (Exception e) {
            e.getStackTrace();
            return null;
        }
    }

    /**
     * 数据预处理,从CSV文件中提取属性集
     * Param:
     * csvReader:读取了CSV文件的Reader,CsvReader
     *
     * return:一个包含的所有属性的属性集，String[]
     * */
    public static String[] Preprocess_extract_Attribute_Set(CsvReader csvReader) throws Exception{
        csvReader.readRecord();
        String Header=csvReader.getRawRecord();
        String[] attribute_set=Header.split(",");
        return attribute_set;
    }

    /**
     * 数据预处理，从CSV文件中读取训练集
     * Param:
     * csvReader:读取了CSV文件的Reader,CsvReader
     *
     * return:一个包含了所有训练数据的数据集，String[]
     * */
    public static String[][] Preprocess_extract_Training_Set(CsvReader csvReader) throws Exception{
        String[][] training_set=new String[17][8];
        int row_index=0;
        while (csvReader.readRecord()){
            String data=csvReader.getRawRecord();
            String[] data_split=data.split(",");
            for(int i=0;i<data_split.length;i++){
                training_set[row_index][i]=data_split[i];
            }
            row_index++;
        }
        return training_set;
    }

    /**
     * 定义以2为底的对数运算，用到换底公式
     * param:
     * X:所要求对数的input,double
     *
     * return:运算后所得的对数,double
     *
     * */
    public static double log2(double X){
        return Math.log(X)/Math.log(2);
    }

    /**
     * 获取某一个属性上的class类别
     * param:
     * Training_Set:用于进行训练的数据集，String[][]
     * Attribute_Set:从CSV文件中获取的属性集，用于判定该属性是否存在于属性集中,String[]
     * attribute_index:根据该属性来对训练集进行分类，int
     *
     * return:训练集在某个属性上得所有class类别集合，String[]
     * */
    public static String[] get_class(String[][] Training_Set, String[] Attribute_Set, int attribute_index){
        ArrayList<String> Value_Set=new ArrayList<String>();
        int attribute_line_count=attribute_index;
        Value_Set.add(Training_Set[0][attribute_line_count]);
        boolean if_add=true;
        for(int i=0;i<Training_Set.length;i++) {
            if_add=true;
            for(int j=0;j<Value_Set.size();j++){
                if(Training_Set[i][attribute_line_count].equals(Value_Set.get(j))) {
                    if_add=false;
                }
            }
            if(if_add==true)
                Value_Set.add(Training_Set[i][attribute_line_count]);
        }
        String[] classes=new String[Value_Set.size()];
        for(int i=0;i<Value_Set.size();i++){
            classes[i]=Value_Set.get(i);
        }
        return classes;
    }

    /**
     * 获取某一个属性上的class_prob
     * param:
     * Training_Set:用于进行训练的数据集，String[][]
     * Attribute_Set:从CSV文件中获取的属性集，用于判定该属性是否存在于属性集中,String[]
     * attribute_index:根据该属性来对训练集进行分类，int
     *
     * return:训练集在某个属性上得所有class_prob集合，int[]
     * */
    public static int[] get_class_prob(String[][] Training_Set, String[] Attribute_Set, int attribute_index){
        String[] Value_Set=get_class(Training_Set,Attribute_Set,attribute_index);
        int [] class_prob=new int[Value_Set.length];
        for(int i=0;i<Training_Set.length;i++){
            for(int j=0;j<Value_Set.length;j++) {
                if (Training_Set[i][attribute_index].equals(Value_Set[j])){
                    class_prob[j]++;
                }
            }
        }
        return class_prob;
    }

    /**
     * 计算information entropy
     * param:
     * Training_Set:用于进行训练的数据集，String[][]
     * Attribute_Set:从CSV文件中获取的属性集，用于判定该属性是否存在于属性集中,String[]
     *
     * return：该数据集在某个index属性上取得的信息熵,double
     *
     * */
    public static double get_Information_entropy(String[][] Training_Set,String[] Attribute_Set){
        double ie=0.0;
        int[] class_prob=get_class_prob(Training_Set,Attribute_Set,Attribute_Set.length);
        for (int i=0;i<class_prob.length;i++){
            double class_prob_percent=class_prob[i]/Double.parseDouble(Training_Set.length+"");
            ie+=class_prob_percent*log2(class_prob_percent);
        }
        return -ie;
    }

    /**
     * 计算information Gain
     * param:
     * Training_Set:用于进行训练的数据集，String[][]
     * Attribute_Set:从CSV文件中获取的属性集，用于判定该属性是否存在于属性集中,String[]
     * attribute_index:根据该属性来对训练集进行分类，int
     * information_entropy:该数据集在某个index属性上取得的信息熵,double
     *
     * return：该数据集在某个index属性上取得的信息增益,double
     * */
    public static double get_Information_Gain(String[][] Training_Set,String[] Attribute_Set,int attribute_index,double information_entropy){
        double ig=0.0;

        String[] results=get_class(Training_Set,Attribute_Set,Attribute_Set.length);
        int[] result_prob=get_class_prob(Training_Set,Attribute_Set,Attribute_Set.length);
        int[] class_prob=get_class_prob(Training_Set,Attribute_Set,attribute_index);
        String[] classes=get_class(Training_Set,Attribute_Set,attribute_index);
        for(int i=0;i<classes.length;i++){
            int pos=0;
            int neg=0;
            String[][] sub_Training_Set=new String[class_prob[i]][Attribute_Set.length];
            int sub_index=0;
            for(int j=0;j<Training_Set.length;j++){
                if(Training_Set[j][attribute_index].equals(classes[i])){
                    sub_Training_Set[sub_index]=Training_Set[j];
                    sub_index++;
                    if(Training_Set[j][Attribute_Set.length].equals(results[0])) {
                        pos++;
                    }
                }
            }
            //System.out.println(get_Information_entropy(sub_Training_Set,Attribute_Set));
            ig+=(class_prob[i]/Double.parseDouble(Training_Set.length+""))*get_Information_entropy(sub_Training_Set,Attribute_Set);
            //System.out.println(classes[i]+":"+class_prob[i]+" pos:"+pos+" neg:"+(class_prob[i]-pos)+"　entropy:"+(class_prob[i]/Double.parseDouble(Training_Set.length+""))*get_Information_entropy(sub_Training_Set,Attribute_Set));
        }
        ig=information_entropy-ig;
        return ig;
    }

    /**
     * 根据information Gain获取最优划分属性
     * param:
     * ig_list:一个包含了所有属性信息增益的列表,double[]
     *
     * return:最优划分属性的index
     * */
    public static int get_best_attribute(String[][] Training_Set,String[] Attribute_Set){
        //计算整个集合的information entropy
        double information_entropy=get_Information_entropy(Training_Set,Attribute_Set);
        //计算每个属性上的information gain,除去"编号"和"好瓜"
        double[] imformation_gain_list=new double[Attribute_Set.length];
        for(int i=1;i<Attribute_Set.length-1;i++){
            double information_gain=get_Information_Gain(Training_Set,Attribute_Set,i,information_entropy);
            imformation_gain_list[i]=information_gain;
        }
        double best_ig=0.0;
        int best_ig_index=0;
        for(int i=0;i<imformation_gain_list.length;i++){
            if(imformation_gain_list[i]>=best_ig){
                best_ig=imformation_gain_list[i];
                best_ig_index=i;
            }
        }
        return best_ig_index;
    }

    /**
     * 判断当前training_set中的样本是否属于同一个类别
     * param:
     * Training_Set:数据训练集，String[][]
     *
     * return :如果样本同属一个类别，则返回类别，否则返回""，String
     * */
    public static String if_all_classes_same(String[][] Training_Set,String[] Attribute_Set){
        for(int i=0;i<Training_Set.length-1;i++){
            if(!Training_Set[i][Attribute_Set.length-1].equals(Training_Set[i+1][Attribute_Set.length-1]))
                return "";
        }
        return Training_Set[0][Attribute_Set.length-1];
    }

    /**
     * 判断当前traning_set中的样本在属性的取值上是否完全相同
     * param:
     * Training_Set:数据训练集，String[][]
     * Attribute_Set:数据属性集,String[]
     *
     * return:如果样本取值完全相同，则返回样本数最多的类名，否则返回“”，String
     * */
    public static String if_all_attribute_same(String Training_Set[][],String[] Attribute_Set){
        for(int i=0;i<Attribute_Set.length;i++) {
            if(get_class(Training_Set,Attribute_Set,i).length!=1){
                return "";
            }
        }
        int[] classes=get_class_prob(Training_Set,Attribute_Set,Attribute_Set.length);
        int big=classes[0];
        for(int i=0;i<classes.length;i++){
            if(classes[i]>big)
                big=classes[i];
        }
        return get_class(Training_Set,Attribute_Set,Attribute_Set.length)[big];
    }

    /**
     * 构建决策树
     * Param:
     * Training_Set:数据训练集，String[][]
     * Attribute_Set:数据属性集，String[]
     *
     * return:以Tree_Node为根节点的一颗Decision Tree
     * */
    public static Tree_Node Tree_Generator(String[][] Traning_Set,String[] Attribute_Set,Tree_Node tree_node){
        //如果Traning_Set中的样本全部都属于同一个类别，则将当前node标注为叶结点
        if(!if_all_classes_same(Traning_Set,Attribute_Set).equals("")){
            tree_node.setIf_leaves_node(if_all_classes_same(Traning_Set,Attribute_Set));
            return tree_node;
        }
        //如果Attribute_Set不为空，或者Traning_Set中样本在Attribute_Set上的取值完全相同,
        //则将当前node标记为叶结点，类别标记为Traning_Set样本数最多的类
        if(Attribute_Set.length!=0 || if_all_attribute_same(Traning_Set,Attribute_Set)!=""){
            tree_node.setIf_leaves_node(if_all_attribute_same(Traning_Set,Attribute_Set));
            return tree_node;
        }
        //如果均不为以上两种情况
        //从Attribute_Set中选取最优划分属性
        int best_devide_attribute=get_best_attribute(Traning_Set,Attribute_Set);
        //获取最优属性上各个样本的属性值
        String[] best_classes=get_class(Traning_Set,Attribute_Set,best_devide_attribute);
        tree_node.setSelf_Key(Attribute_Set[best_devide_attribute]);
        for(int i=0;i<best_classes.length;i++){
            //为当前的node生成分支
            Tree_Node node=new Tree_Node();
            node.setParent_Key(Attribute_Set[best_devide_attribute]);
            node.setParent_Value(best_classes[i]);
            if(get_class_prob(Traning_Set,Attribute_Set,i).length==0){
                node.setIf_leaves_node(if_all_attribute_same(Traning_Set,Attribute_Set));
                return node;
            }
            else{
                return Tree_Generator(make_subDataSet(Traning_Set,Attribute_Set,best_devide_attribute,i),make_subAttributeSet(Traning_Set,Attribute_Set,best_devide_attribute),node);
            }
        }
        return null;
    }

    /**
     * divide操作，用于从Training_Set中去除一列
     * param:
     * Training_Set:训练集,String[][]
     * Attribute_Set:属性集，String[]
     * drop_index:去除的列编号,int
     *
     * return:去除了列编号的subset,String[][]
     * */
    public static String[] make_subAttributeSet(String[][] Training_Set,String[] Attribute_Set,int drop_index){
        String[] subset=new String[Attribute_Set.length];
        for(int i=0;i<Attribute_Set.length;i++){
            if(i<drop_index)
                subset[i]=Attribute_Set[i];
            else if(i>drop_index)
                subset[i]=Attribute_Set[i+1];
            else
                continue;
        }
        return subset;
    }

    /**
     * divide操作，用于从Training_Set中保留具有特定属性上value值的行
     * param:
     * Training_Set:训练集,String[][]
     * Attribute_Set:属性集，String[]
     * attribute_index:保留的行属性的编号，int
     * stayline_index:保留的行属性值的编号,int
     *
     * return:保留了特定编号的subset,String[][]
     * */
    public static String[][] make_subDataSet(String[][] Training_Set,String[] Attribute_Set,int attribute_index,int stayline_index){
        int linenum=get_class_prob(Training_Set,Attribute_Set,attribute_index)[stayline_index];
        String classname=get_class(Training_Set,Attribute_Set,attribute_index)[stayline_index];
        String[][] subset=new String[linenum][Attribute_Set.length];
        for(int i=0,k=0;i<Training_Set.length;i++){
            if(Training_Set[i][attribute_index].equals(classname))
                subset[k]=Training_Set[i];
        }
        return subset;
    }

    public static void main(String[] args) throws Exception{
        CsvReader reader=Read_data_from_csv("F:/java_project/Decision_Tree/data/melon_data.csv");
        //准备属性集
        String[] Attribute_Set=Preprocess_extract_Attribute_Set(reader);
        //准备训练集
        String[][] Training_Set=Preprocess_extract_Training_Set(reader);
        Tree_Generator(Training_Set,Attribute_Set,new Tree_Node());
    }
}
