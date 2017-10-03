package treebolic.provider.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import treebolic.glue.Color;
import treebolic.model.IEdge;
import treebolic.model.MenuItem;
import treebolic.model.Model;
import treebolic.model.MountPoint;
import treebolic.model.Mounter;
import treebolic.model.MutableEdge;
import treebolic.model.MutableNode;
import treebolic.model.Settings;
import treebolic.model.Tree;
import treebolic.model.Utils;
import treebolic.provider.IProvider;

/**
 * Document adapter to model/graph
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class DocumentAdapter
{
	// D A T A

	/**
	 * Mount task (protracted)
	 *
	 * @author Bernard Bou
	 */
	class MountTask
	{
		/**
		 * Mount point
		 */
		public final MountPoint.Mounting theMountPoint;

		/**
		 * Node to mount graph at
		 */
		public final MutableNode theMountingNode;

		/**
		 * Constructor
		 *
		 * @param thisMountPoint   mount point
		 * @param thisMountingNode Node to mount graph at
		 */
		public MountTask(final MountPoint.Mounting thisMountPoint, final MutableNode thisMountingNode)
		{
			this.theMountPoint = thisMountPoint;
			this.theMountingNode = thisMountingNode;
		}

		/**
		 * Perform task
		 *
		 * @param theseEdges edges in grafting tree
		 */
		@SuppressWarnings("synthetic-access")
		public void run(final List<IEdge> theseEdges)
		{
			if (DocumentAdapter.this.theProvider == null)
			{
				System.err.println("Mount not performed: " + this.theMountPoint + " @ " + this.theMountingNode); //$NON-NLS-1$//$NON-NLS-2$
				return;
			}
			final Tree thisTree = DocumentAdapter.this.theProvider.makeTree(this.theMountPoint.theURL, DocumentAdapter.this.theBase, DocumentAdapter.this.theParameters, true);
			if (thisTree != null)
			{
				Mounter.graft(this.theMountingNode, thisTree.getRoot(), theseEdges, thisTree.getEdges());
			}
		}
	}

	/**
	 * Protracted mount tasks
	 */
	private List<MountTask> theMountTasks = null;

	/**
	 * Provider (used to generate mounted trees)
	 */
	private final IProvider theProvider;

	/**
	 * Base
	 */
	private final URL theBase;

	/**
	 * Parameters
	 */
	private final Properties theParameters;

	/**
	 * Id to node map
	 */
	private Map<String, MutableNode> theIdToNodeMap;

	// C O N S T R U C T O R

	/**
	 * Constructor
	 *
	 * @param thisProvider    provider (used in recursion)
	 * @param thisBase        base
	 * @param theseParameters parameters
	 */
	public DocumentAdapter(final IProvider thisProvider, final URL thisBase, final Properties theseParameters)
	{
		this.theProvider = thisProvider;
		this.theBase = thisBase;
		this.theParameters = theseParameters;
	}

	/**
	 * Constructor
	 */
	public DocumentAdapter()
	{
		this(null, null, null);
	}

	// A C C E S S

	/**
	 * Get id to node map
	 *
	 * @return id to node map
	 */
	public Map<String, MutableNode> getIdToNodeMap()
	{
		return this.theIdToNodeMap;
	}

	// M A K E

	/**
	 * Make model from document
	 *
	 * @param thisDocument document
	 * @return model
	 */
	public Model makeModel(final Document thisDocument)
	{
		this.theIdToNodeMap = new TreeMap<>();
		final Tree thisTree = toTree(thisDocument);
		if (thisTree == null)
		{
			return null;
		}
		final Settings theseSettings = DocumentAdapter.toSettings(thisDocument);
		return new Model(thisTree, theseSettings);
	}

	/**
	 * Make tree from document
	 *
	 * @param thisDocument document
	 * @return tree
	 */
	public Tree makeTree(final Document thisDocument)
	{
		this.theIdToNodeMap = new Hashtable<>();
		return toTree(thisDocument);
	}

	// P A R S E

	/**
	 * Make model node
	 *
	 * @param thisParent model parent
	 * @param thisId     id
	 * @return model node
	 */
	@SuppressWarnings("WeakerAccess")
	protected MutableNode makeNode(final MutableNode thisParent, final String thisId)
	{
		return new MutableNode(thisParent, thisId);
	}

	/**
	 * Make model edge
	 *
	 * @param thisFromNode model from-node end
	 * @param thisToNode   model to-node end
	 * @return model edge
	 */
	@SuppressWarnings("WeakerAccess")
	protected MutableEdge makeEdge(final MutableNode thisFromNode, final MutableNode thisToNode)
	{
		return new MutableEdge(thisFromNode, thisToNode);
	}

	/**
	 * Make graph
	 *
	 * @param thisDocument document
	 * @return graph
	 */
	private Tree toTree(final Document thisDocument)
	{
		// nodes
		final Element thisRootElement = DocumentAdapter.getFirstElementByTagName(thisDocument.getDocumentElement(), "node"); //$NON-NLS-1$
		if (thisRootElement == null)
		{
			return null;
		}
		final MutableNode thisRoot = toNode(thisRootElement, null);

		// edges
		final List<IEdge> theseEdges = toEdges(thisDocument);

		// run protracted mount tasks (had to be protracted until edges become available)
		if (this.theMountTasks != null)
		{
			for (final MountTask thisTask : this.theMountTasks)
			{
				thisTask.run(theseEdges);
			}
			this.theMountTasks.clear();
			this.theMountTasks = null;
		}

		return new Tree(thisRoot, theseEdges);
	}

	/**
	 * Make model node
	 *
	 * @param thisNodeElement starting DOM element
	 * @param thisParent      model parent node
	 * @return model node
	 */
	private MutableNode toNode(final Element thisNodeElement, final MutableNode thisParent)
	{
		// id
		final String thisId = thisNodeElement.getAttribute("id"); //$NON-NLS-1$

		// make
		final MutableNode thisNode = makeNode(thisParent, thisId);
		this.theIdToNodeMap.put(thisId, thisNode);

		// colors
		final Color thisBackColor = Utils.stringToColor(thisNodeElement.getAttribute("backcolor")); //$NON-NLS-1$
		thisNode.setBackColor(thisBackColor);
		final Color thisForeColor = Utils.stringToColor(thisNodeElement.getAttribute("forecolor")); //$NON-NLS-1$
		thisNode.setForeColor(thisForeColor);

		// weight
		final String thisWeight = thisNodeElement.getAttribute("weight"); //$NON-NLS-1$
		if (thisWeight != null && !thisWeight.isEmpty())
		{
			thisNode.setWeight(-Double.parseDouble(thisWeight));
		}

		// label
		Element thisElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisNodeElement, "label"); //$NON-NLS-1$
		if (thisElement != null)
		{
			final String thisLabel = thisElement.getTextContent();
			if (thisLabel != null && !thisLabel.isEmpty())
			{
				thisNode.setLabel(thisLabel);
			}
		}

		// image
		String thisImageSrc = null;
		thisElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisNodeElement, "img"); //$NON-NLS-1$
		if (thisElement != null)
		{
			thisImageSrc = thisElement.getAttribute("src"); //$NON-NLS-1$
			if (thisImageSrc != null && !thisImageSrc.isEmpty())
			{
				thisNode.setImageFile(thisImageSrc);
			}
		}

		// content
		thisElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisNodeElement, "content"); //$NON-NLS-1$
		if (thisElement != null)
		{
			final String thisContent = thisElement.getTextContent();
			if (thisContent != null && !thisContent.isEmpty())
			{
				if (thisImageSrc != null && !thisImageSrc.isEmpty())
				{
					String sb = "<p><img src='" + //
							thisImageSrc + //
							"' style='float:left;margin-right:10px;'/>" + //
							thisContent + //
							"</p>";
					/*
					sb.append("<table><tr><td valign='top'><img src='");
					sb.append(thisImageSrc);
					sb.append("'/></td><td>");
					sb.append(thisContent);
					sb.append("</td></tr></table>");
					*/
					thisNode.setContent(sb);
				}
				else
				{
					thisNode.setContent(thisContent);
				}

			}
		}

		// tree.edge
		thisElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisNodeElement, "treeedge"); //$NON-NLS-1$
		if (thisElement != null)
		{
			// label
			final Element thisLabelElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisElement, "label"); //$NON-NLS-1$
			if (thisLabelElement != null)
			{
				final String thisLabel = thisLabelElement.getTextContent();
				if (thisLabel != null && !thisLabel.isEmpty())
				{
					thisNode.setEdgeLabel(thisLabel);
				}
			}

			// image
			final Element thisImageElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisElement, "img"); //$NON-NLS-1$
			if (thisImageElement != null)
			{
				final String thisEdgeImageSrc = thisImageElement.getAttribute("src"); //$NON-NLS-1$
				if (thisEdgeImageSrc != null && !thisEdgeImageSrc.isEmpty())
				{
					thisNode.setEdgeImageFile(thisEdgeImageSrc);
				}
			}

			// color
			final Color thisColor = Utils.stringToColor(thisElement.getAttribute("color")); //$NON-NLS-1$
			if (thisColor != null)
			{
				thisNode.setEdgeColor(thisColor);
			}

			// style
			final Integer thisStyle = Utils.parseStyle(thisElement.getAttribute("stroke"), thisElement.getAttribute("fromterminator"), thisElement.getAttribute("toterminator"), thisElement.getAttribute("line"), thisElement.getAttribute("hidden")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			if (thisStyle != null)
			{
				thisNode.setEdgeStyle(thisStyle);
			}
		}

		// link
		thisElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisNodeElement, "a"); //$NON-NLS-1$
		if (thisElement != null)
		{
			final String thisHref = thisElement.getAttribute("href"); //$NON-NLS-1$
			if (thisHref != null && !thisHref.isEmpty())
			{
				thisNode.setLink(thisHref);
			}
		}

		// mount
		thisElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisNodeElement, "mountpoint"); //$NON-NLS-1$
		if (thisElement != null)
		{
			final Element thisAElement = DocumentAdapter.getFirstElementByTagName(thisElement, "a"); //$NON-NLS-1$
			if (thisAElement != null)
			{
				final String thisHref = thisAElement.getAttribute("href"); //$NON-NLS-1$
				if (thisHref != null && !thisHref.isEmpty())
				{
					final MountPoint.Mounting thisMountPoint = new MountPoint.Mounting();
					thisMountPoint.theURL = thisHref;

					// mount now ?
					final String thisValue = thisElement.getAttribute("now"); //$NON-NLS-1$
					if (thisValue != null && !thisValue.isEmpty() && Boolean.valueOf(thisValue))
					{
						thisMountPoint.now = true;

						final MountTask thisTask = new MountTask(thisMountPoint, thisNode);
						if (this.theMountTasks == null)
						{
							this.theMountTasks = new ArrayList<>();
						}
						this.theMountTasks.add(thisTask);
					}
					thisNode.setMountPoint(thisMountPoint);
				}
			}
		}

		// recurse to children
		final List<Element> theseChildElements = DocumentAdapter.getLevel1ChildElementsByTagName(thisNodeElement, "node"); //$NON-NLS-1$
		for (final Element thisChildElement : theseChildElements)
		{
			toNode(thisChildElement, thisNode);
		}

		return thisNode;
	}

	/**
	 * Make model edge
	 *
	 * @param thisEdgeElement edge DOM element
	 * @return edge
	 */
	private MutableEdge toEdge(final Element thisEdgeElement)
	{
		final String thisFromId = thisEdgeElement.getAttribute("from"); //$NON-NLS-1$
		final String thisToId = thisEdgeElement.getAttribute("to"); //$NON-NLS-1$
		final MutableNode thisFromNode = this.theIdToNodeMap.get(thisFromId);
		final MutableNode thisToNode = this.theIdToNodeMap.get(thisToId);
		final MutableEdge thisEdge = makeEdge(thisFromNode, thisToNode);

		// label
		final Element thisLabelElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisEdgeElement, "label"); //$NON-NLS-1$
		if (thisLabelElement != null)
		{
			final String thisLabel = thisLabelElement.getTextContent();
			if (thisLabel != null && !thisLabel.isEmpty())
			{
				thisEdge.setLabel(thisLabel);
			}
		}

		// image
		final Element thisImageElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisEdgeElement, "img"); //$NON-NLS-1$
		if (thisImageElement != null)
		{
			final String thisImageSrc = thisImageElement.getAttribute("src"); //$NON-NLS-1$
			if (thisImageSrc != null && !thisImageSrc.isEmpty())
			{
				thisEdge.setImageFile(thisImageSrc);
			}
		}

		// color
		final Color thisColor = Utils.stringToColor(thisEdgeElement.getAttribute("color")); //$NON-NLS-1$
		if (thisColor != null)
		{
			thisEdge.setColor(thisColor);
		}

		// style
		final Integer thisStyle = Utils.parseStyle(thisEdgeElement.getAttribute("stroke"), thisEdgeElement.getAttribute("fromterminator"), thisEdgeElement.getAttribute("toterminator"), thisEdgeElement.getAttribute("line"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				thisEdgeElement.getAttribute("hidden")); //$NON-NLS-1$
		if (thisStyle != null)
		{
			thisEdge.setStyle(thisStyle);
		}
		return thisEdge;
	}

	/**
	 * Make list of model edges
	 *
	 * @param thisDocument DOM document
	 * @return list of edges
	 */
	private List<IEdge> toEdges(final Document thisDocument)
	{
		List<IEdge> thisEdgeList = null;
		final NodeList theseChildren = thisDocument.getElementsByTagName("edge"); //$NON-NLS-1$
		for (int i = 0; i < theseChildren.getLength(); i++)
		{
			final Node thisNode = theseChildren.item(i);
			final Element thisEdgeElement = (Element) thisNode;
			final MutableEdge thisEdge = toEdge(thisEdgeElement);
			if (thisEdgeList == null)
			{
				thisEdgeList = new ArrayList<>();
			}
			thisEdgeList.add(thisEdge);
		}
		return thisEdgeList;
	}

	/**
	 * Make settings
	 *
	 * @param thisDocument DOM document
	 * @return settings
	 */
	static private Settings toSettings(final Document thisDocument)
	{
		final Settings theseSettings = new Settings();

		// T O P
		Element thisElement = thisDocument.getDocumentElement();
		if (thisElement != null && thisElement.getNodeName().equals("treebolic")) //$NON-NLS-1$
		{
			String thisAttribute = thisElement.getAttribute("toolbar"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theHasToolbarFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("statusbar"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theHasStatusbarFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("popupmenu"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theHasPopUpMenuFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("tooltip"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theHasToolTipFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("tooltip-displays-content"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theToolTipDisplaysContentFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("focus"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theFocus = thisAttribute;
			}
			thisAttribute = thisElement.getAttribute("focus-on-hover"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theFocusOnHoverFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("xmoveto"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theXMoveTo = Float.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("ymoveto"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theYMoveTo = Float.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("xshift"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theXShift = Float.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("yshift"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theYShift = Float.valueOf(thisAttribute);
			}
		}

		// T R E E
		thisElement = DocumentAdapter.getFirstElementByTagName(thisDocument.getDocumentElement(), "tree"); //$NON-NLS-1$
		if (thisElement != null)
		{
			// img
			final Element thisImageElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisElement, "img"); //$NON-NLS-1$
			if (thisImageElement != null)
			{
				final String thisSrc = thisImageElement.getAttribute("src"); //$NON-NLS-1$
				if (thisSrc != null && !thisSrc.isEmpty())
				{
					theseSettings.theBackgroundImageFile = thisSrc;
				}
			}

			// colors
			Color thisColor = Utils.stringToColor(thisElement.getAttribute("backcolor")); //$NON-NLS-1$
			if (thisColor != null)
			{
				theseSettings.theBackColor = thisColor;
			}
			thisColor = Utils.stringToColor(thisElement.getAttribute("forecolor")); //$NON-NLS-1$
			if (thisColor != null)
			{
				theseSettings.theForeColor = thisColor;
			}

			// attributes
			String thisAttribute;
			thisAttribute = thisElement.getAttribute("orientation"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theOrientation = thisAttribute;
			}
			thisAttribute = thisElement.getAttribute("expansion"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theExpansion = Float.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("sweep"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theSweep = Float.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("preserve-orientation"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.thePreserveOrientationFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("fontface"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theFontFace = thisAttribute;
			}
			thisAttribute = thisElement.getAttribute("fontsize"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theFontSize = Integer.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("scalefonts"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theDownscaleFontsFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("fontscaler"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theFontDownscaler = Utils.stringToFloats(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("scaleimages"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theDownscaleImagesFlag = Boolean.valueOf(thisAttribute);
			}
			thisAttribute = thisElement.getAttribute("imagescaler"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theImageDownscaler = Utils.stringToFloats(thisAttribute);
			}
		}

		// N O D E S
		thisElement = DocumentAdapter.getFirstElementByTagName(thisDocument.getDocumentElement(), "nodes"); //$NON-NLS-1$
		if (thisElement != null)
		{
			// img
			final Element thisImageElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisElement, "img"); //$NON-NLS-1$
			if (thisImageElement != null)
			{
				final String thisSrc = thisImageElement.getAttribute("src"); //$NON-NLS-1$
				if (thisSrc != null && !thisSrc.isEmpty())
				{
					theseSettings.theDefaultNodeImage = thisSrc;
				}
			}

			// colors
			Color thisColor = Utils.stringToColor(thisElement.getAttribute("backcolor")); //$NON-NLS-1$
			if (thisColor != null)
			{
				theseSettings.theNodeBackColor = thisColor;
			}
			thisColor = Utils.stringToColor(thisElement.getAttribute("forecolor")); //$NON-NLS-1$
			if (thisColor != null)
			{
				theseSettings.theNodeForeColor = thisColor;
			}

			// label
			String thisAttribute = thisElement.getAttribute("border"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theBorderFlag = Boolean.valueOf(thisAttribute);
			}

			thisAttribute = thisElement.getAttribute("ellipsize"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theEllipsizeFlag = Boolean.valueOf(thisAttribute);
			}
		}

		// E D G E S
		thisElement = DocumentAdapter.getFirstElementByTagName(thisDocument.getDocumentElement(), "edges"); //$NON-NLS-1$
		if (thisElement != null)
		{
			// img
			final Element thisImageElement = DocumentAdapter.getFirstLevel1ElementByTagName(thisElement, "img"); //$NON-NLS-1$
			if (thisImageElement != null)
			{
				final String thisSrc = thisImageElement.getAttribute("src"); //$NON-NLS-1$
				if (thisSrc != null && !thisSrc.isEmpty())
				{
					theseSettings.theDefaultEdgeImage = thisSrc;
				}
			}

			// arc
			final String thisAttribute = thisElement.getAttribute("arc"); //$NON-NLS-1$
			if (thisAttribute != null && !thisAttribute.isEmpty())
			{
				theseSettings.theEdgesAsArcsFlag = Boolean.valueOf(thisAttribute);
			}
		}

		// D E F A U L T . T R E E . E D G E
		thisElement = DocumentAdapter.getFirstElementByTagName(thisDocument.getDocumentElement(), "default.treeedge"); //$NON-NLS-1$
		if (thisElement != null)
		{
			// img
			final Element thisImageElement = DocumentAdapter.getFirstElementByTagName(thisElement, "img"); //$NON-NLS-1$
			if (thisImageElement != null)
			{
				final String thisSrc = thisImageElement.getAttribute("src"); //$NON-NLS-1$
				if (thisSrc != null && !thisSrc.isEmpty())
				{
					theseSettings.theDefaultTreeEdgeImage = thisSrc;
				}
			}

			// color
			final Color thisColor = Utils.stringToColor(thisElement.getAttribute("color")); //$NON-NLS-1$
			if (thisColor != null)
			{
				theseSettings.theTreeEdgeColor = thisColor;
			}

			// style
			final Integer thisStyle = Utils.parseStyle(thisElement.getAttribute("stroke"), thisElement.getAttribute("fromterminator"), thisElement.getAttribute("toterminator"), thisElement.getAttribute("line"), thisElement.getAttribute("hidden")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			if (thisStyle != null)
			{
				theseSettings.theTreeEdgeStyle = thisStyle;
			}
		}

		// D E F A U L T . E D G E
		thisElement = DocumentAdapter.getFirstElementByTagName(thisDocument.getDocumentElement(), "default.edge"); //$NON-NLS-1$
		if (thisElement != null)
		{
			// img
			final Element thisImageElement = DocumentAdapter.getFirstElementByTagName(thisElement, "img"); //$NON-NLS-1$
			if (thisImageElement != null)
			{
				final String thisSrc = thisImageElement.getAttribute("src"); //$NON-NLS-1$
				if (thisSrc != null && !thisSrc.isEmpty())
				{
					theseSettings.theDefaultEdgeImage = thisSrc;
				}
			}

			// color
			final Color thisColor = Utils.stringToColor(thisElement.getAttribute("color")); //$NON-NLS-1$
			if (thisColor != null)
			{
				theseSettings.theEdgeColor = thisColor;
			}

			// style
			final Integer thisStyle = Utils.parseStyle(thisElement.getAttribute("stroke"), thisElement.getAttribute("fromterminator"), thisElement.getAttribute("toterminator"), thisElement.getAttribute("line"), thisElement.getAttribute("hidden")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			if (thisStyle != null)
			{
				theseSettings.theEdgeStyle = thisStyle;
			}
		}

		// D E F A U L T . E D G E
		List<MenuItem> thisMenuItemList = null;
		final NodeList theseChildren = thisDocument.getElementsByTagName("menuitem"); //$NON-NLS-1$
		for (int i = 0; i < theseChildren.getLength(); i++)
		{
			final Node thisNode = theseChildren.item(i);
			thisElement = (Element) thisNode;
			final MenuItem thisMenuItem = DocumentAdapter.toMenuItem(thisElement);

			if (thisMenuItemList == null)
			{
				thisMenuItemList = new ArrayList<>();
			}
			thisMenuItemList.add(thisMenuItem);
		}
		theseSettings.theMenu = thisMenuItemList;

		return theseSettings;
	}

	/**
	 * Make menu item
	 *
	 * @param thisElement menu item DOM element
	 * @return menu item
	 */
	static private MenuItem toMenuItem(final Element thisElement)
	{
		final MenuItem thisMenuItem = new MenuItem();
		Utils.parseMenuItem(thisMenuItem, thisElement.getAttribute("action"), thisElement.getAttribute("match-scope"), thisElement.getAttribute("match-mode")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// match target
		thisMenuItem.theMatchTarget = thisElement.getAttribute("match-target");

		// label
		final Element thisLabelElement = DocumentAdapter.getFirstElementByTagName(thisElement, "label"); //$NON-NLS-1$
		if (thisLabelElement != null)
		{
			thisMenuItem.theLabel = thisLabelElement.getTextContent();
		}

		// link
		final Element thisLinkElement = DocumentAdapter.getFirstElementByTagName(thisElement, "a"); //$NON-NLS-1$
		if (thisLinkElement != null)
		{
			thisMenuItem.theLink = thisLinkElement.getAttribute("href"); //$NON-NLS-1$
			thisMenuItem.theTarget = thisLinkElement.getAttribute("target"); //$NON-NLS-1$
		}

		return thisMenuItem;
	}

	// H E L P E R S

	/**
	 * Find DOM element with given tag
	 *
	 * @param thisElement starting DOM element
	 * @param thisTagName tag
	 * @return DOM element if found, null if none
	 */
	static private Element getFirstElementByTagName(final Element thisElement, final String thisTagName)
	{
		if (thisElement != null)
		{
			final NodeList thisList = thisElement.getElementsByTagName(thisTagName);
			if (thisList.getLength() > 0)
			{
				return (Element) thisList.item(0);
			}
		}
		return null;
	}

	/**
	 * Find DOM element with given tag among first level children
	 *
	 * @param thisElement starting DOM element
	 * @param thisTagName tag
	 * @return DOM element if found, null if none
	 */
	static private Element getFirstLevel1ElementByTagName(final Element thisElement, final String thisTagName)
	{
		final List<Element> theseChildElements = DocumentAdapter.getLevel1ChildElementsByTagName(thisElement, thisTagName);
		if (!theseChildElements.isEmpty())
		{
			return theseChildElements.get(0);
		}
		return null;
	}

	/**
	 * Find DOM elements with given tag among first level children
	 *
	 * @param thisElement starting DOM element
	 * @param thisTagName tag
	 * @return DOM element if found, null if none
	 */
	static private List<Element> getLevel1ChildElementsByTagName(final Element thisElement, final String thisTagName)
	{
		final ArrayList<Element> thisList = new ArrayList<>();
		final NodeList theseChildren = thisElement.getChildNodes();
		for (int i = 0; i < theseChildren.getLength(); i++)
		{
			final Node thisNode = theseChildren.item(i);
			if (thisNode instanceof Element)
			{
				final Element thisChildElement = (Element) thisNode;
				if (thisChildElement.getTagName().equals(thisTagName))
				{
					thisList.add(thisChildElement);
				}
			}
		}
		return thisList;
	}
}
