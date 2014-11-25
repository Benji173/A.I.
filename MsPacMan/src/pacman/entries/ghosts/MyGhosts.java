package pacman.entries.ghosts;

import java.util.EnumMap;
import java.util.Random;

import javax.swing.text.Document;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getActions() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.ghosts.mypackage).
 */
public class MyGhosts extends Controller<EnumMap<GHOST,MOVE>>
{
	private final static float CONSISTENCY=1.0f;	//carry out intended move with this probability
	private Random rnd=new Random();
	private EnumMap<GHOST,MOVE> myMoves=new EnumMap<GHOST,MOVE>(GHOST.class);
	private MOVE[] moves=MOVE.values();
	private String myState = "chase";
	private String myEvent = "null";
	private String myAction;
	/* (non-Javadoc)
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */
	
	public String FSM(String currState, String evt){
		 try {
			 
				File fXmlFile = new File("C:/Users/lab422/Desktop/A.I/MsPacMan/src/XML's/staff2.xml");
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);
			 
				(doc).getDocumentElement().normalize();
			 
				NodeList nList = doc.getElementsByTagName("staff");
			 
				for (int temp = 0; temp < nList.getLength(); temp++) {
			 
					Node nNode = nList.item(temp);
					Element eElement = (Element) nNode;
					if (eElement.getElementsByTagName("currState").item(0).getTextContent().equals(currState) 
							&& eElement.getElementsByTagName("evt").item(0).getTextContent().equals(evt) ) {
						return eElement.getElementsByTagName("newState").item(0).getTextContent();	 
					}
				}
			    } catch (Exception e) {
				e.printStackTrace();
			    }
			    return currState;
	}
	
	public EnumMap<GHOST,MOVE> getMove(Game game,long timeDue)
	{		
		myMoves.clear();
		myState = FSM("wander","close");
		for(GHOST ghost : GHOST.values())				//for each ghost
			if(game.doesGhostRequireAction(ghost))		//if it requires an action
			{	
				int d = game.getManhattanDistance(game.getGhostCurrentNodeIndex(ghost),game.getPacmanCurrentNodeIndex());
				if(game.getManhattanDistance(game.getGhostCurrentNodeIndex(ghost), game.getPacmanCurrentNodeIndex())< 50){
					myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(game.getGhostCurrentNodeIndex(ghost),
							game.getPacmanCurrentNodeIndex(),game.getGhostLastMoveMade(ghost),DM.PATH));
				}
				else{
					int currentNodeIndex=game.getGhostCurrentNodeIndex(ghost);	
					int[] activePills=game.getActivePillsIndices();
					int[] activePowerPills=game.getActivePowerPillsIndices();
					int[] targetNodeIndices=new int[activePills.length+activePowerPills.length];
					
					for(int i=0;i<activePills.length;i++)
						targetNodeIndices[i]=activePills[i];
					
					for(int i=0;i<activePowerPills.length;i++)
						targetNodeIndices[activePills.length+i]=activePowerPills[i];
					
					int nearest = game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(),targetNodeIndices,DM.PATH);
					myMoves.put(ghost, game.getNextMoveTowardsTarget(currentNodeIndex,nearest,DM.PATH));//
				}
				
			}

		return myMoves;
	}
}