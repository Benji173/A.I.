package pacman.entries.ghosts;

import java.io.File;
import java.util.EnumMap;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/*
 * This is the class you need to modify for your entry. In particular, you need to
 * fill in the getActions() method. Any additional classes you write should either
 * be placed in this package or sub-packages (e.g., game.entries.ghosts.mypackage).
 */
public class MyGhosts extends Controller<EnumMap<GHOST, MOVE>> {
	public static final int CROWDED_DISTANCE = 30;
	public static final int PACMAN_DISTANCE = 10;
	public static final int PILL_PROXIMITY = 15;

	private final static float CONSISTENCY = 1.0f; // carry out intended move
													// with this probability
	private Random rnd = new Random();
	private EnumMap<GHOST, MOVE> myMoves = new EnumMap<GHOST, MOVE>(GHOST.class);
	private final EnumMap<GHOST, Integer> cornerAllocation = new EnumMap<GHOST, Integer>(
			GHOST.class);
	private MOVE[] moves = MOVE.values();
	private String myState = "chase";
	private String myEvent = "none";
	private String myAction = "chase";

	/*
	 * (non-Javadoc)
	 * 
	 * @see pacman.controllers.Controller#getMove(pacman.game.Game, long)
	 */

	public MyGhosts() {
		cornerAllocation.put(GHOST.BLINKY, 0);
		cornerAllocation.put(GHOST.INKY, 1);
		cornerAllocation.put(GHOST.PINKY, 2);
		cornerAllocation.put(GHOST.SUE, 3);
	}

	public void FSM() {
		try {

			File fXmlFile = new File(
					"C:/Users/lab422/Desktop/A.I/MsPacMan/src/XML's/FSM.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(fXmlFile);

			(doc).getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("FSM");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				Element eElement = (Element) nNode;
				if (eElement.getElementsByTagName("currState").item(0)
						.getTextContent().equals(myState)
						&& eElement.getElementsByTagName("evt").item(0)
								.getTextContent().equals(myEvent)) {
					myAction = eElement.getElementsByTagName("action").item(0)
							.getTextContent();
					myState = eElement.getElementsByTagName("newState").item(0)
							.getTextContent();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		
		FSM();
		
		int pacmanIndex=game.getPacmanCurrentNodeIndex();
		
		for (GHOST ghost : GHOST.values())
			// for each ghost
			if (game.doesGhostRequireAction(ghost)) // if it requires an action
			{
				int currentIndex=game.getGhostCurrentNodeIndex(ghost);
				//if crowed and not close to pacman, disperse
				if(myAction.equals("disperse"))
        			myMoves.put(ghost,getRetreatActions(game,ghost));                          				//go towards the power pill locations
        		//if edible or Ms Pac-Man is close to power pill, move away from Ms Pac-Man
        		else if(myAction.equals("close"))
        			myMoves.put(ghost,game.getApproximateNextMoveAwayFromTarget(currentIndex,pacmanIndex,game.getGhostLastMoveMade(ghost),DM.PATH));      			//move away from ms pacman
        		//else go towards Ms Pac-Man
        		else if( myAction.equals("chase"))      		
        			myMoves.put(ghost,game.getApproximateNextMoveTowardsTarget(currentIndex,pacmanIndex,game.getGhostLastMoveMade(ghost),DM.PATH));
				
				if(isCrowded(game) && !closeToMsPacMan(game,currentIndex)){
					myEvent = "crowed and not close";
				}
				else if(game.getGhostEdibleTime(ghost)>0 || closeToPower(game)){
					myEvent = "edible or close";
				}
				else if(myState == "runaway" && game.getGhostEdibleTime(ghost)==0){
					myEvent = "not edible";
				}
				else
					myEvent = "none";
			}

		return myMoves;
	}

	private boolean closeToPower(Game game) {
		int pacmanIndex = game.getPacmanCurrentNodeIndex();
		int[] powerPillIndices = game.getActivePowerPillsIndices();

		for (int i = 0; i < powerPillIndices.length; i++)
			if (game.getShortestPathDistance(powerPillIndices[i], pacmanIndex) < PILL_PROXIMITY)
				return true;

		return false;
	}

	private boolean closeToMsPacMan(Game game, int location) {
		if (game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),
				location) < PACMAN_DISTANCE)
			return true;

		return false;
	}

	private boolean isCrowded(Game game) {
		GHOST[] ghosts = GHOST.values();
		float distance = 0;

		for (int i = 0; i < ghosts.length - 1; i++)
			for (int j = i + 1; j < ghosts.length; j++)
				distance += game.getShortestPathDistance(
						game.getGhostCurrentNodeIndex(ghosts[i]),
						game.getGhostCurrentNodeIndex(ghosts[j]));

		return (distance / 6) < CROWDED_DISTANCE ? true : false;
	}

	private MOVE getRetreatActions(Game game, GHOST ghost) {
		int currentIndex = game.getGhostCurrentNodeIndex(ghost);
		int pacManIndex = game.getPacmanCurrentNodeIndex();

		if (game.getGhostEdibleTime(ghost) == 0
				&& game.getShortestPathDistance(currentIndex, pacManIndex) < PACMAN_DISTANCE)
			return game.getApproximateNextMoveTowardsTarget(currentIndex,
					pacManIndex, game.getGhostLastMoveMade(ghost), DM.PATH);
		else
			return game.getApproximateNextMoveTowardsTarget(currentIndex,
					game.getPowerPillIndices()[cornerAllocation.get(ghost)],
					game.getGhostLastMoveMade(ghost), DM.PATH);
	}
}
