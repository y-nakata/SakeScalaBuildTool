package sake

import org.specs._ 

object ProjectSpec extends Specification {
    
    object project extends Project
    
    import sake.targets._
    import sake.util._
    
    def verifyDeps(t: Target, deps: List[Symbol]) = {
        t.dependencies.size must be_==(deps.size)
        for (i <- 0 to (deps.size - 1)) {
            t.dependencies(i) must be_==(deps(i))
        }
    }
    
    "The target method" should {
        "accept a symbol as its name as the first parameter" in {
            val group = project.target('targetName)
            group.targets.length must be_==(1)
            val t = group.targets.head
            t.name must be_==('targetName)
            verifyDeps(t, Nil)
        }

        "accept a string as its name as the first parameter" in {
            val group = project.target("targetName")
            group.targets.length must be_==(1)
            val t = group.targets.head
            t.name must be_==('targetName)
            verifyDeps(t, Nil)
        }

        "convert an input string name to a symbol" in {
            val group = project.target("targetName")
            group.targets.length must be_==(1)
            val t = group.targets.head
            t.name must be_==('targetName)
            verifyDeps(t, Nil)
        }
        
        "accept more than one symbol and/or string as the names of new targets, as the first parameter" in {
            val group = project.target('targetName1, "targetName2", "targetName3", 'targetName4)
            group.targets.length must be_==(4)
            val t = group.targets.head
            List('targetName1, 'targetName2, 'targetName3, 'targetName4).foreach { n =>
                group.targets.find(t => t.name == n) match {
                    case None => fail(n.toString())
                    case Some(t) => verifyDeps(t, Nil)
                }
            }
        }

        "accept a Nil list of names and create no new targets" in {
            val group = project.target(Nil)
            group.targets.length must be_==(0)
        } 

        """accept a list of one or more symbols and/or strings as the names of new targets, 
            as the first parameter""" in {
            val targs = List('targetName1, "targetName2", "targetName3", 'targetName4)
            val group = project.target(targs)
            group.targets.length must be_==(4)
            List('targetName1, 'targetName2, 'targetName3, 'targetName4).foreach { n =>
                group.targets.find(t => t.name == n) match {
                    case None => fail(n.toString())
                    case Some(t) => verifyDeps(t, Nil)
                }
            }
        }

        "accept a single dependent after each name (and convert it to a List)" in {
            val group = project.target('targetName1 -> 'dep11, "targetName2" -> "dep21")
            group.targets.length must be_==(2)
            Map('targetName1 -> List('dep11), 'targetName2 -> List('dep21)).foreach { n_d => 
                group.targets.find(t => t.name == n_d._1) match {
                    case None => fail(n_d._1.toString())
                    case Some(t) => verifyDeps(t, n_d._2)
                }
            }
        }

        "accept a List of dependents after each name" in {
            val group = project.target('targetName1 -> List('dep11, "dep12"), "targetName2" -> ("dep21" :: 'dep22 :: Nil))
            group.targets.length must be_==(2)
            Map('targetName1 -> List('dep11, 'dep12), 'targetName2 -> List('dep21, 'dep22)).foreach { n_d => 
                group.targets.find(t => t.name == n_d._1) match {
                    case None => fail(n_d._1.toString())
                    case Some(t) => verifyDeps(t, n_d._2)
                }
            }
        }


        "accept a List of dependents after a list of name, where each target gets the same list of dependencies" in {
            val group = project.target(List('targetName1, "targetName2") -> List('depa, "depb", 'depc))
            group.targets.length must be_==(2)
            List('targetName1, 'targetName2).foreach { n =>
                group.targets.find(t => t.name == n) match {
                    case None => fail(n.toString())
                    case Some(t) => verifyDeps(t, List('depa, 'depb, 'depc))
                }
            }
        }
    }
    
    "The target method with an action" should {
        "accept the action as a no-argument closure returning Unit" in {
            var invoked = 0
            val group = project.target('targetName) { 
                invoked += 1
            }
            val t = group.targets.head
            t.build()
            t.build()
            invoked must be_==(2)
        }

        "include the target name in the exception message if the build fails" in {
            var invoked = 0
            val group = project.target('FailedTarget) { 
                throw new BuildError("test")
            }
            val t = group.targets.head
            try {
                t.build()
            } catch {
                case BuildError(m, th) => m.contains("FailedTarget") must be_==(true)
                case _ => fail()
            }
        }
    }
}