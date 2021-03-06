package ca.uwaterloo.cs451.project


import io.bespin.scala.util.Tokenizer

import org.apache.log4j._
import org.apache.hadoop.fs._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.rogach.scallop._

class testConf(args: Seq[String]) extends ScallopConf(args) with Tokenizer {
  mainOptions = Seq(input, output)
  val input = opt[String](descr = "input path", required = false)
  val output = opt[String](descr = "output path", required = false)
//   val reducers = opt[Int](descr = "number of reducers", required = false, default = Some(1))
//   val numExecutors = opt[Int](descr = "number of executors", required = false, default = Some(1))
//   val executorCores = opt[Int](descr = "number of cores", required = false, default = Some(1))
//   val threshold = opt[Int](descr = "threshold", required = false, default = Some(10))
  verify()
}

class SimpleCSVHeader(header:Array[String]) extends Serializable {
  val index = header.zipWithIndex.toMap
  def apply(array:Array[String], key:String):String = array(index(key))
}

object test extends Tokenizer {
  val log = Logger.getLogger(getClass().getName())

  def main(argv: Array[String]) {
    val args = new testConf(argv)

//     log.info("Input: " + args.input())
//     log.info("Output: " + args.output())
//     log.info("Number of reducers: " + args.reducers())

    val conf = new SparkConf().setAppName("test")
    val sc = new SparkContext(conf)
//     FileSystem.get(sc.hadoopConfiguration).delete("numberOfProducts", true)

//     val outputDir = new Path(args.output())
//     FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    
//     Date received	Product	Sub-product	Issue	Sub-issue	
//     Consumer complaint narrative	Company public response	Company	
//     State	ZIP code	Tags	Consumer consent provided?	Submitted via	
//     Date sent to company	Company response to consumer	Timely response?	
//     Consumer disputed?	Complaint ID
    
    var outputDir = new Path("numberOfProducts")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("productsOfCompanies")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("StatesConsumerDisputed")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("ProductDispute")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("HowSubmitted")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("StateTimelyResponse")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("YearlyDispute")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("HowTimelyResponse")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)
    outputDir = new Path("HowConsumerDisputed")
    FileSystem.get(sc.hadoopConfiguration).delete(outputDir, true)



    var textFile = sc.textFile("ConsumerComplaints.txt")
    
    
    var csv = textFile
    .map(line => {
      val tokens = line.split("\\t").toList
      (tokens, tokens.length)
    })
    .filter(line => {
      line._2 == 18
    })
    
    csv
    .map(line => (line._1(1), 1))
    .reduceByKey(_+_)
    .sortByKey()
    .map(a => {
      (a._1 + "$" + a._2.toString)
    })
    .coalesce(1,true)
    .saveAsTextFile("numberOfProducts")
    
    csv
    .map(line => {
      ((line._1(7), line._1(1)),1)
    })
    .reduceByKey(_+_)
    .sortByKey()
    .map(a => {
      (a._1._1 + "$" + a._1._2 + "$" + a._2.toString)
    }) 
    .coalesce(1,true)
    .saveAsTextFile("productsOfCompanies")
    
    csv
    .map(line => {
        var yes = 0
        var no = 0
        var na = 0
        if(line._1(16) == "Yes") yes = 1
        else if(line._1(16) == "No") no = 1
        else na = 1
        (line._1(8), (yes, no, na))
    })
    .reduceByKey((v1,v2) => {
      (v1._1 + v2._1, v1._2 + v2._2, v1._3 + v2._3)
    })
    .sortByKey()
    .coalesce(1,true)
    .saveAsTextFile("StatesConsumerDisputed")
    
    csv
    .map(line => {
        var email = 0
        var fax = 0
        var phone = 0
        var post = 0
        var ref = 0
        var web = 0
        if(line._1(12) == "Email") email = 1
        else if(line._1(12) == "Fax") fax = 1
        else if(line._1(12) == "Phone") phone = 1
        else if(line._1(12) == "Postal mail") post = 1
        else if(line._1(12) == "Referral") ref = 1
        else web = 1
      (line._1(8), (email, fax, phone, post, ref, web))
    })
    .reduceByKey((v1,v2) => {
      (v1._1 + v2._1, v1._2 + v2._2, v1._3 + v2._3, v1._4 + v2._4, v1._5 + v2._5, v1._6 + v2._6)
    })
    .sortByKey()
    .coalesce(1,true)
    .saveAsTextFile("HowSubmitted")
    
    csv
    .map(line => {
        var yes = 0
        var no = 0
        var na = 0
        if(line._1(16) == "Yes") yes = 1
        else if(line._1(16) == "No") no = 1
        else na = 1
        (line._1(1), (yes, no, na))
    })
    .reduceByKey((v1,v2) => {
      (v1._1 + v2._1, v1._2 + v2._2, v1._3 + v2._3)
    })
    .sortByKey()
    .coalesce(1,true)
    .saveAsTextFile("ProductDispute")
    
    csv
    .map(line => {
        var yes = 0
        var no = 0
        if(line._1(15) == "Yes") yes = 1
        else no = 1
        (line._1(8), (yes, no))
    })
    .reduceByKey((v1,v2) => {
      (v1._1 + v2._1, v1._2 + v2._2)
    })
    .sortByKey()
    .coalesce(1,true)
    .saveAsTextFile("StateTimelyResponse")
    
    csv
    .map(line => {
      var year = line._1(0).substring(6,10)
      var prod = line._1(1)
      var yes = 0
      var no = 0
      var na = 0
      if(line._1(16) == "Yes") yes = 1
      else if(line._1(16) == "No") no = 1
      else na = 1
      ((prod + "$" + year),(yes, no, na))
    })
      .reduceByKey((v1, v2) => {
        (v1._1 + v2._1, v1._2 + v2._2, v1._3 + v2._3)
      })
      .sortByKey()
      .coalesce(1,true)
      .saveAsTextFile("YearlyDispute")
    
    csv
    .map(line => {
        var yes = 0
        var no = 0
        if(line._1(15) == "Yes") yes = 1
        else no = 1
        (line._1(12), (yes, no))
    })
    .reduceByKey((v1,v2) => {
      (v1._1 + v2._1, v1._2 + v2._2)
    })
    .sortByKey()
    .coalesce(1,true)
    .saveAsTextFile("HowTimelyResponse")
    
    csv
    .map(line => {
        var yes = 0
        var no = 0
        var na = 0
        if(line._1(16) == "Yes") yes = 1
        else if(line._1(16) == "No") no = 1
        else na = 1
        (line._1(12), (yes, no, na))
    })
    .reduceByKey((v1,v2) => {
      (v1._1 + v2._1, v1._2 + v2._2, v1._3 + v2._3)
    })
    .sortByKey()
    .coalesce(1,true)
    .saveAsTextFile("HowConsumerDisputed")
  }
}
