package ammonite

import scala.io.Source
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.apache.spark.rdd.RDD

import org.apache.spark.SparkFiles

object Arbitrage {
    val DATA_URL = "https://fx.priceonomics.com/v1/rates/"
    val htmlSource = Source.fromURL(DATA_URL).mkString

    val spark = SparkSession.builder.master("local[*]").appName("Nice App").getOrCreate()
    val sc = spark.sparkContext

    def vertexId(string: String) = string.hashCode.toLong
    def vertexIdPair(string: String) = (string.hashCode.toLong, string)

    def rawFields(): Array[Array[String]] = htmlSource.substring(2,htmlSource.length-2).split("\", \"").map(a=> a.split("\": \"").map(c=> c.split("_")).flatten)
    def allCurrencyNames(): Array[Array[String]] = for( Array(f1,f2,f3) <- rawFields) yield Array(f1,f2)

    def getNodeNames(): Array[String] = allCurrencyNames.flatten.distinct
    def getNodePairs():Array[(Long, String)] = getNodeNames.map(a=> (vertexId(a),a))
    val currencies: RDD[(VertexId, (String))] = sc.parallelize(getNodePairs)

    def getRelationArray():  Array[Edge[Double]] = for(Array(from, to, value) <- rawFields) yield Edge(vertexId(from), vertexId(to), value.toDouble)
    val relationships = sc.parallelize(getRelationArray)
    val graph = Graph(currencies, relationships)
    val res = graph.mapVertices((_, att) => att.toUpperCase())

    def listOfCurrencies() = res.vertices.collect().toList
    def prepareResultData(list: List[(VertexId, Double)] ) = list.map(x=> (listOfCurrencies.filter(f=> f._1.compare(x._1) == 0)(0)._2, x._2))

    def sparkItUp() = {
        val graphWithNoNegativeCycles = Graph(currencies, relationships)
        var result:List[String] = List()
        graphWithNoNegativeCycles.vertices.collect.toList.foreach(node => {
            val t = BellmanFord.getStuff(graphWithNoNegativeCycles, node._1.toLong)
            println( "{" + Range(1,t.length).map(x => prepareResultData(t)(x-1)._1 + "_" +  prepareResultData(t)(x)._1 + ": " + prepareResultData(t)(x)._2  ).toList.mkString(", ") + "}")
            result = result :+  "{" + Range(1,t.length).map(x => prepareResultData(t)(x-1)._1 + "_" +  prepareResultData(t)(x)._1 + ": " + prepareResultData(t)(x)._2  ).toList.mkString(", ") + "}"
        } )
        println(htmlSource)
    }
}

object BellmanFord {
    def updateList(list: List[(VertexId, Double)], node: (Long,Double)):  List[(VertexId, Double)] = {
        var list1:List[(VertexId, Double)] = list.filter(x=> x._1 != node._1)
        list1 = list1:+ node
        return list1
    }

    def getStuff(graph: Graph[String,Double], src:Long): List[(VertexId, Double)] = {
        val V:Long = graph.vertices.count
        val E:Long = graph.edges.count

        var dist: List[(VertexId, Double)] = List()
        dist = graph.vertices.collect.map(x => (x._1, Double.MaxValue)).toList

        dist = updateList(dist,(src,0.toDouble))

        Range(1,V.toInt).foreach( i => {
            graph.edges.collect.foreach( edge => {
                var u:Long = edge.srcId
                var v:Long = edge.dstId
                var weight:Double = edge.attr
                val listRecord = dist.filter(x=> x._1.compare(u) == 0 ) 
                val listRecordO = dist.filter(x=> x._1.compare(v) == 0 ) 
                if (listRecord.length != 0 && listRecord(0)._2 + weight < listRecordO(0)._2) {
                    dist = updateList(dist,(v, (listRecord(0)._2 + weight).toDouble))
                }
            })
        })
    
        graph.edges.collect.foreach(edge => {
            val u:Long = edge.srcId
            val v:Long = edge.dstId
            val weight:Double = edge.attr
            val listRecord = dist.filter( x => x._1.compare(u) == 0 ) 
            val listRecordO = dist.filter( x => x._1.compare(v) == 0 ) 
            if (listRecord.length != 0 && listRecord(0)._2 + weight < listRecordO(0)._2) {
                System.out.println("Graph contains negative weight cycle")
            }
        })

        return dist
    
    }
}
