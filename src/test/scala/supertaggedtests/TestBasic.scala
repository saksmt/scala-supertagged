package supertaggedtests


import org.scalatest._
import shapeless.test.illTyped


class TestBasic extends FlatSpec with Matchers {



  "`TopLevel` type tagging" should "work" in {

    illTyped("""testUser1(userString)""", "type mismatch;.+")
    illTyped("""val user3 = UserInt(userString)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")


    val user1_0 = User1(userString)
    val user1_1 = User1 @@ userString

    testUser1(user1_0)
    testUser1(user1_1)


    testUser1(User1(userString))
    testUser1(User1 @@ userString)
    testUser1(User1 @@ "userString")

    illTyped("""testUser1(userString)""", "type mismatch;.+")
    illTyped("""testUser2(user1_0)""", "type mismatch;.+")


    /** Complex **/

    val array = ArrayOfInt( Array(1,2,3) )

    testArrayOfIntRaw(array)
    testArrayOfInt(array)
    illTyped("""testArrayOfString(array)""", "type mismatch;.+")


    testArrayOfInt(ArrayOfInt(Array(1,2,3)))
    testArrayOfInt(ArrayOfInt @@ Array(1,2,3))


    //Doesn't compile. Scalac BUG (polymorphic expression...), not critical see notes in README
    //    testArrayOfIntRaw(ArrayOfInt(Array(1,2,3)))
    //    testArrayOfIntRaw(ArrayOfInt @@ Array(1,2,3))

  }


  "`Nested` type tagging" should "work for deep `List`" in {

    val userList_1_lvl = User1(userStrings_1_lvl)
    val userList_2_lvl = User1(userStrings_2_lvl)
    val userList_3_lvl = User1(userStrings_3_lvl)
    val userList_4_lvl = User1(userStrings_4_lvl)
    val userList_5_lvl = User1(userStrings_5_lvl)

    val userList_10_lvl = User1(List(List(List(List(List(userStrings_5_lvl))))))

    illTyped("""UserInt(userStrings_1_lvl)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")
    illTyped("""UserInt(userStrings_2_lvl)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")
    illTyped("""UserInt(userStrings_3_lvl)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")
    illTyped("""UserInt(userStrings_4_lvl)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")
    illTyped("""UserInt(userStrings_5_lvl)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")



    val head1 = userList_1_lvl.head
    testUser1(head1)
    testUser1(userList_1_lvl.head)


    val head2 = userList_2_lvl.head.head
    testUser1(head2)


    val head3 = userList_3_lvl.head.head.head
    testUser1(head3)


    val head4 = userList_4_lvl.head.head.head.head
    testUser1(head4)


    val head5 = userList_5_lvl.head.head.head.head.head
    testUser1(head5)

    val head10 = userList_10_lvl.head.head.head.head.head.head.head.head.head.head
    testUser1(head10)
    testUser1(userList_10_lvl.head.head.head.head.head.head.head.head.head.head)

    // 9 times `.head`
    illTyped("""testUser1(userList_10_lvl.head.head.head.head.head.head.head.head.head)""", "type mismatch;.+")



    /** middle-nested **/

    val value = ArrayArrayOfInt @@ List(List(List(Array(Array(1,2,3)))))
    val valueHead = value.head.head.head

    testArrayArrayOfIntRaw(valueHead)
    testArrayArrayOfInt(valueHead)
    illTyped("""testArrayArrayOfInt2(valueHead)""","type mismatch;.+")



    /** crazy-middle-nested **/
    illTyped("CrazyNestedCounters @@ List(List(List(Array(Array(1,2,3)))))","could not find implicit value for parameter tagger: supertagged.Tagger.+")

    val crazyArray = Array(Array(Array(Array(Array(1,2,3)))))
    val crazy0 = Counter @@ crazyArray
    val crazy1 = Counters @@ crazy0
    val crazy2 = CrazyNestedCounters @@ crazy1

    testCrazyCounters(crazy2.head.head.head) shouldBe 1
    testCounter(crazy2.head.head.head.head.head)

    /**
      * Since Tag[T,+U] is universal trait, code below is not an issue any more
      */
    //    try{
//        crazy2.head.head.head.head.head
//
//      throw new RuntimeException("It should fail!")
//    }catch{
//      case e:ClassCastException if e.getMessage == "[I cannot be cast to [Ljava.lang.Object;" =>
//        println("JVM feature working!")
//    }

  }


  "`Nested` tagging" should "work for deep `Array`" in {
    val userStrings_1_lvl = Array(userString)
    val userStrings_5_lvl = Array(Array(Array(Array(Array(userString)))))

    val userList_1_lvl = User1(userStrings_1_lvl)
    val userList_5_lvl = User1(userStrings_5_lvl)

    illTyped("""UserInt(userStrings_1_lvl)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")
    illTyped("""UserInt(userStrings_5_lvl)""", "could not find implicit value for parameter tagger: supertagged.Tagger.+")


    val head1 = userList_1_lvl.head
    testUser1(head1)

    val head5 = userList_5_lvl.head.head.head.head.head
    testUser1(head5)


    testUser1( (User1 @@ userStrings_5_lvl).head.head.head.head.head )
  }




  "Replace SINGLE tag for `TopLevel` " should "work" in {

    val user1 = User1(userString)
    testUser1(user1)

    val user2replaced1 = User2 !@@ user1

    testUser2(user2replaced1)
    illTyped("""testUser1(user2replaced1)""", "type mismatch;.+")
  }

  "Replace SINGLE tag for `Nested`" should "work" in {

    val userList_5_lvl = User2 @@ userStrings_5_lvl
    val user1replaced2 = User1 !@@ userList_5_lvl


    val head = user1replaced2.head.head.head.head.head
    testUser1(head)
    illTyped("""testUser2(head)""", "type mismatch;.+")


    //Even if there is no tags
    val user1replaced0 = User1 !@@ userStrings_5_lvl

    val head2 = user1replaced0.head.head.head.head.head
    testUser1(head2)
  }


}
