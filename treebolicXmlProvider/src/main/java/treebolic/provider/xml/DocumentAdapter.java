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
		public final MountPoint.Mounting mountPoint;

		/**
		 * Node to mount graph at
		 */
		public final MutableNode mountingNode;

		/**
		 * Constructor
		 *
		 * @param mountPoint   mount point
		 * @param mountingNode Node to mount graph at
		 */
		public MountTask(final MountPoint.Mounting mountPoint, final MutableNode mountingNode)
		{
			this.mountPoint = mountPoint;
			this.mountingNode = mountingNode;
		}

		/**
		 * Perform task
		 *
		 * @param edges edges in grafting tree
		 */
		public void run(final List<IEdge> edges)
		{
			if (DocumentAdapter.this.provider == null)
			{
				System.err.println("Mount not performed: " + this.mountPoint + " @ " + this.mountingNode); //$NON-NLS-1$//$NON-NLS-2$
				return;
			}
			final Tree tree = DocumentAdapter.this.provider.makeTree(this.mountPoint.url, DocumentAdapter.this.base, DocumentAdapter.this.parameters, true);
			if (tree != null)
			{
				Mounter.graft(this.mountingNode, tree.getRoot(), edges, tree.getEdges());
			}
		}
	}

	/**
	 * Protracted mount tasks
	 */
	private List<MountTask> mountTasks = null;

	/**
	 * Provider (used to generate mounted trees)
	 */
	private final IProvider provider;

	/**
	 * Base
	 */
	private final URL base;

	/**
	 * Parameters
	 */
	private final Properties parameters;

	/**
	 * Id to node map
	 */
	private Map<String, MutableNode> idToNodeMap;

	// C O N S T R U C T O R

	/**
	 * Constructor
	 *
	 * @param provider   provider (used in recursion)
	 * @param base       base
	 * @param parameters parameters
	 */
	public DocumentAdapter(final IProvider provider, final URL base, final Properties parameters)
	{
		this.provider = provider;
		this.base = base;
		this.parameters = parameters;
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
		return this.idToNodeMap;
	}

	// M A K E

	/**
	 * Make model from document
	 *
	 * @param document document
	 * @return model
	 */
	public Model makeModel(final Document document)
	{
		this.idToNodeMap = new TreeMap<>();
		final Tree tree = toTree(document);
		if (tree == null)
		{
			return null;
		}
		final Settings settings = DocumentAdapter.toSettings(document);
		return new Model(tree, settings);
	}

	/**
	 * Make tree from document
	 *
	 * @param document document
	 * @return tree
	 */
	public Tree makeTree(final Document document)
	{
		this.idToNodeMap = new Hashtable<>();
		return toTree(document);
	}

	// P A R S E

	/**
	 * Make model node
	 *
	 * @param parent model parent
	 * @param id     id
	 * @return model node
	 */
	@SuppressWarnings("WeakerAccess")
	protected MutableNode makeNode(final MutableNode parent, final String id)
	{
		return new MutableNode(parent, id);
	}

	/**
	 * Make model edge
	 *
	 * @param fromNode model from-node end
	 * @param toNode   model to-node end
	 * @return model edge
	 */
	@SuppressWarnings("WeakerAccess")
	protected MutableEdge makeEdge(final MutableNode fromNode, final MutableNode toNode)
	{
		return new MutableEdge(fromNode, toNode);
	}

	/**
	 * Make graph
	 *
	 * @param document document
	 * @return graph
	 */
	private Tree toTree(final Document document)
	{
		// nodes
		final Element rootElement = DocumentAdapter.getFirstElementByTagName(document.getDocumentElement(), "node"); //$NON-NLS-1$
		if (rootElement == null)
		{
			return null;
		}
		final MutableNode root = toNode(rootElement, null);

		// edges
		final List<IEdge> edges = toEdges(document);

		// run protracted mount tasks (had to be protracted until edges become available)
		if (this.mountTasks != null)
		{
			for (final MountTask task : this.mountTasks)
			{
				task.run(edges);
			}
			this.mountTasks.clear();
			this.mountTasks = null;
		}

		return new Tree(root, edges);
	}

	/**
	 * Make model node
	 *
	 * @param nodeElement starting DOM element
	 * @param parent      model parent node
	 * @return model node
	 */
	private MutableNode toNode(final Element nodeElement, final MutableNode parent)
	{
		// id
		final String id = nodeElement.getAttribute("id"); //$NON-NLS-1$

		// make
		final MutableNode node = makeNode(parent, id);
		this.idToNodeMap.put(id, node);

		// colors
		final Color backColor = Utils.stringToColor(nodeElement.getAttribute("backcolor")); //$NON-NLS-1$
		node.setBackColor(backColor);
		final Color foreColor = Utils.stringToColor(nodeElement.getAttribute("forecolor")); //$NON-NLS-1$
		node.setForeColor(foreColor);

		// weight
		final String weight = nodeElement.getAttribute("weight"); //$NON-NLS-1$
		if (weight != null && !weight.isEmpty())
		{
			node.setWeight(-Double.parseDouble(weight));
		}

		// label
		Element element = DocumentAdapter.getFirstLevel1ElementByTagName(nodeElement, "label"); //$NON-NLS-1$
		if (element != null)
		{
			final String label = element.getTextContent();
			if (label != null && !label.isEmpty())
			{
				node.setLabel(label);
			}
		}

		// image
		String imageSrc = null;
		element = DocumentAdapter.getFirstLevel1ElementByTagName(nodeElement, "img"); //$NON-NLS-1$
		if (element != null)
		{
			imageSrc = element.getAttribute("src"); //$NON-NLS-1$
			if (imageSrc != null && !imageSrc.isEmpty())
			{
				node.setImageFile(imageSrc);
			}
		}

		// content
		element = DocumentAdapter.getFirstLevel1ElementByTagName(nodeElement, "content"); //$NON-NLS-1$
		if (element != null)
		{
			final String content = element.getTextContent();
			if (content != null && !content.isEmpty())
			{
				if (imageSrc != null && !imageSrc.isEmpty())
				{
					String sb = "<p><img src='" + //
							imageSrc + //
							"' style='float:left;margin-right:10px;'/>" + //
							content + //
							"</p>";
					/*
					sb.append("<table><tr><td valign='top'><img src='");
					sb.append(imageSrc);
					sb.append("'/></td><td>");
					sb.append(content);
					sb.append("</td></tr></table>");
					*/
					node.setContent(sb);
				}
				else
				{
					node.setContent(content);
				}
			}
		}

		// tree.edge
		element = DocumentAdapter.getFirstLevel1ElementByTagName(nodeElement, "treeedge"); //$NON-NLS-1$
		if (element != null)
		{
			// label
			final Element labelElement = DocumentAdapter.getFirstLevel1ElementByTagName(element, "label"); //$NON-NLS-1$
			if (labelElement != null)
			{
				final String label = labelElement.getTextContent();
				if (label != null && !label.isEmpty())
				{
					node.setEdgeLabel(label);
				}
			}

			// image
			final Element imageElement = DocumentAdapter.getFirstLevel1ElementByTagName(element, "img"); //$NON-NLS-1$
			if (imageElement != null)
			{
				final String edgeImageSrc = imageElement.getAttribute("src"); //$NON-NLS-1$
				if (edgeImageSrc != null && !edgeImageSrc.isEmpty())
				{
					node.setEdgeImageFile(edgeImageSrc);
				}
			}

			// color
			final Color color = Utils.stringToColor(element.getAttribute("color")); //$NON-NLS-1$
			if (color != null)
			{
				node.setEdgeColor(color);
			}

			// style
			final Integer style = Utils.parseStyle(element.getAttribute("stroke"), element.getAttribute("fromterminator"), element.getAttribute("toterminator"), element.getAttribute("line"), element.getAttribute("hidden")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			if (style != null)
			{
				node.setEdgeStyle(style);
			}
		}

		// link
		element = DocumentAdapter.getFirstLevel1ElementByTagName(nodeElement, "a"); //$NON-NLS-1$
		if (element != null)
		{
			final String href = element.getAttribute("href"); //$NON-NLS-1$
			if (href != null && !href.isEmpty())
			{
				node.setLink(href);
			}
		}

		// mount
		element = DocumentAdapter.getFirstLevel1ElementByTagName(nodeElement, "mountpoint"); //$NON-NLS-1$
		if (element != null)
		{
			final Element aElement = DocumentAdapter.getFirstElementByTagName(element, "a"); //$NON-NLS-1$
			if (aElement != null)
			{
				final String href = aElement.getAttribute("href"); //$NON-NLS-1$
				if (href != null && !href.isEmpty())
				{
					final MountPoint.Mounting mountPoint = new MountPoint.Mounting();
					mountPoint.url = href;

					// mount now ?
					final String value = element.getAttribute("now"); //$NON-NLS-1$
					if (value != null && !value.isEmpty() && Boolean.valueOf(value))
					{
						mountPoint.now = true;

						final MountTask task = new MountTask(mountPoint, node);
						if (this.mountTasks == null)
						{
							this.mountTasks = new ArrayList<>();
						}
						this.mountTasks.add(task);
					}
					node.setMountPoint(mountPoint);
				}
			}
		}

		// recurse to children
		final List<Element> childElements = DocumentAdapter.getLevel1ChildElementsByTagName(nodeElement, "node"); //$NON-NLS-1$
		for (final Element childElement : childElements)
		{
			toNode(childElement, node);
		}

		return node;
	}

	/**
	 * Make model edge
	 *
	 * @param edgeElement edge DOM element
	 * @return edge
	 */
	private MutableEdge toEdge(final Element edgeElement)
	{
		final String fromId = edgeElement.getAttribute("from"); //$NON-NLS-1$
		final String toId = edgeElement.getAttribute("to"); //$NON-NLS-1$
		final MutableNode fromNode = this.idToNodeMap.get(fromId);
		final MutableNode toNode = this.idToNodeMap.get(toId);
		final MutableEdge edge = makeEdge(fromNode, toNode);

		// label
		final Element labelElement = DocumentAdapter.getFirstLevel1ElementByTagName(edgeElement, "label"); //$NON-NLS-1$
		if (labelElement != null)
		{
			final String label = labelElement.getTextContent();
			if (label != null && !label.isEmpty())
			{
				edge.setLabel(label);
			}
		}

		// image
		final Element imageElement = DocumentAdapter.getFirstLevel1ElementByTagName(edgeElement, "img"); //$NON-NLS-1$
		if (imageElement != null)
		{
			final String imageSrc = imageElement.getAttribute("src"); //$NON-NLS-1$
			if (imageSrc != null && !imageSrc.isEmpty())
			{
				edge.setImageFile(imageSrc);
			}
		}

		// color
		final Color color = Utils.stringToColor(edgeElement.getAttribute("color")); //$NON-NLS-1$
		if (color != null)
		{
			edge.setColor(color);
		}

		// style
		final Integer style = Utils.parseStyle(edgeElement.getAttribute("stroke"), edgeElement.getAttribute("fromterminator"), edgeElement.getAttribute("toterminator"), edgeElement.getAttribute("line"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				edgeElement.getAttribute("hidden")); //$NON-NLS-1$
		if (style != null)
		{
			edge.setStyle(style);
		}
		return edge;
	}

	/**
	 * Make list of model edges
	 *
	 * @param document DOM document
	 * @return list of edges
	 */
	private List<IEdge> toEdges(final Document document)
	{
		List<IEdge> edgeList = null;
		final NodeList children = document.getElementsByTagName("edge"); //$NON-NLS-1$
		for (int i = 0; i < children.getLength(); i++)
		{
			final Node node = children.item(i);
			final Element edgeElement = (Element) node;
			final MutableEdge edge = toEdge(edgeElement);
			if (edgeList == null)
			{
				edgeList = new ArrayList<>();
			}
			edgeList.add(edge);
		}
		return edgeList;
	}

	/**
	 * Make settings
	 *
	 * @param document DOM document
	 * @return settings
	 */
	static private Settings toSettings(final Document document)
	{
		final Settings settings = new Settings();

		// T O P
		Element element = document.getDocumentElement();
		if (element != null && element.getNodeName().equals("treebolic")) //$NON-NLS-1$
		{
			String attribute = element.getAttribute("toolbar"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.hasToolbarFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("statusbar"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.hasStatusbarFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("popupmenu"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.hasPopUpMenuFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("tooltip"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.hasToolTipFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("tooltip-displays-content"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.toolTipDisplaysContentFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("focus"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.focus = attribute;
			}
			attribute = element.getAttribute("focus-on-hover"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.focusOnHoverFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("xmoveto"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.xMoveTo = Float.valueOf(attribute);
			}
			attribute = element.getAttribute("ymoveto"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.yMoveTo = Float.valueOf(attribute);
			}
			attribute = element.getAttribute("xshift"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.xShift = Float.valueOf(attribute);
			}
			attribute = element.getAttribute("yshift"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.yShift = Float.valueOf(attribute);
			}
		}

		// T R E E
		element = DocumentAdapter.getFirstElementByTagName(document.getDocumentElement(), "tree"); //$NON-NLS-1$
		if (element != null)
		{
			// img
			final Element imageElement = DocumentAdapter.getFirstLevel1ElementByTagName(element, "img"); //$NON-NLS-1$
			if (imageElement != null)
			{
				final String src = imageElement.getAttribute("src"); //$NON-NLS-1$
				if (src != null && !src.isEmpty())
				{
					settings.backgroundImageFile = src;
				}
			}

			// colors
			Color color = Utils.stringToColor(element.getAttribute("backcolor")); //$NON-NLS-1$
			if (color != null)
			{
				settings.backColor = color;
			}
			color = Utils.stringToColor(element.getAttribute("forecolor")); //$NON-NLS-1$
			if (color != null)
			{
				settings.foreColor = color;
			}

			// attributes
			String attribute;
			attribute = element.getAttribute("orientation"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.orientation = attribute;
			}
			attribute = element.getAttribute("expansion"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.expansion = Float.valueOf(attribute);
			}
			attribute = element.getAttribute("sweep"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.sweep = Float.valueOf(attribute);
			}
			attribute = element.getAttribute("preserve-orientation"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.preserveOrientationFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("fontface"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.fontFace = attribute;
			}
			attribute = element.getAttribute("fontsize"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.fontSize = Integer.valueOf(attribute);
			}
			attribute = element.getAttribute("scalefonts"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.downscaleFontsFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("fontscaler"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.fontDownscaler = Utils.stringToFloats(attribute);
			}
			attribute = element.getAttribute("scaleimages"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.downscaleImagesFlag = Boolean.valueOf(attribute);
			}
			attribute = element.getAttribute("imagescaler"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.imageDownscaler = Utils.stringToFloats(attribute);
			}
		}

		// N O D E S
		element = DocumentAdapter.getFirstElementByTagName(document.getDocumentElement(), "nodes"); //$NON-NLS-1$
		if (element != null)
		{
			// img
			final Element imageElement = DocumentAdapter.getFirstLevel1ElementByTagName(element, "img"); //$NON-NLS-1$
			if (imageElement != null)
			{
				final String src = imageElement.getAttribute("src"); //$NON-NLS-1$
				if (src != null && !src.isEmpty())
				{
					settings.defaultNodeImage = src;
				}
			}

			// colors
			Color color = Utils.stringToColor(element.getAttribute("backcolor")); //$NON-NLS-1$
			if (color != null)
			{
				settings.nodeBackColor = color;
			}
			color = Utils.stringToColor(element.getAttribute("forecolor")); //$NON-NLS-1$
			if (color != null)
			{
				settings.nodeForeColor = color;
			}

			// label
			String attribute = element.getAttribute("border"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.borderFlag = Boolean.valueOf(attribute);
			}

			attribute = element.getAttribute("ellipsize"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.ellipsizeFlag = Boolean.valueOf(attribute);
			}
		}

		// E D G E S
		element = DocumentAdapter.getFirstElementByTagName(document.getDocumentElement(), "edges"); //$NON-NLS-1$
		if (element != null)
		{
			// img
			final Element imageElement = DocumentAdapter.getFirstLevel1ElementByTagName(element, "img"); //$NON-NLS-1$
			if (imageElement != null)
			{
				final String src = imageElement.getAttribute("src"); //$NON-NLS-1$
				if (src != null && !src.isEmpty())
				{
					settings.defaultEdgeImage = src;
				}
			}

			// arc
			final String attribute = element.getAttribute("arc"); //$NON-NLS-1$
			if (attribute != null && !attribute.isEmpty())
			{
				settings.edgesAsArcsFlag = Boolean.valueOf(attribute);
			}
		}

		// D E F A U L T . T R E E . E D G E
		element = DocumentAdapter.getFirstElementByTagName(document.getDocumentElement(), "default.treeedge"); //$NON-NLS-1$
		if (element != null)
		{
			// img
			final Element imageElement = DocumentAdapter.getFirstElementByTagName(element, "img"); //$NON-NLS-1$
			if (imageElement != null)
			{
				final String src = imageElement.getAttribute("src"); //$NON-NLS-1$
				if (src != null && !src.isEmpty())
				{
					settings.defaultTreeEdgeImage = src;
				}
			}

			// color
			final Color color = Utils.stringToColor(element.getAttribute("color")); //$NON-NLS-1$
			if (color != null)
			{
				settings.treeEdgeColor = color;
			}

			// style
			final Integer style = Utils.parseStyle(element.getAttribute("stroke"), element.getAttribute("fromterminator"), element.getAttribute("toterminator"), element.getAttribute("line"), element.getAttribute("hidden")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			if (style != null)
			{
				settings.treeEdgeStyle = style;
			}
		}

		// D E F A U L T . E D G E
		element = DocumentAdapter.getFirstElementByTagName(document.getDocumentElement(), "default.edge"); //$NON-NLS-1$
		if (element != null)
		{
			// img
			final Element imageElement = DocumentAdapter.getFirstElementByTagName(element, "img"); //$NON-NLS-1$
			if (imageElement != null)
			{
				final String src = imageElement.getAttribute("src"); //$NON-NLS-1$
				if (src != null && !src.isEmpty())
				{
					settings.defaultEdgeImage = src;
				}
			}

			// color
			final Color color = Utils.stringToColor(element.getAttribute("color")); //$NON-NLS-1$
			if (color != null)
			{
				settings.edgeColor = color;
			}

			// style
			final Integer style = Utils.parseStyle(element.getAttribute("stroke"), element.getAttribute("fromterminator"), element.getAttribute("toterminator"), element.getAttribute("line"), element.getAttribute("hidden")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			if (style != null)
			{
				settings.edgeStyle = style;
			}
		}

		// D E F A U L T . E D G E
		List<MenuItem> menuItemList = null;
		final NodeList children = document.getElementsByTagName("menuitem"); //$NON-NLS-1$
		for (int i = 0; i < children.getLength(); i++)
		{
			final Node node = children.item(i);
			element = (Element) node;
			final MenuItem menuItem = DocumentAdapter.toMenuItem(element);

			if (menuItemList == null)
			{
				menuItemList = new ArrayList<>();
			}
			menuItemList.add(menuItem);
		}
		settings.menu = menuItemList;

		return settings;
	}

	/**
	 * Make menu item
	 *
	 * @param element menu item DOM element
	 * @return menu item
	 */
	static private MenuItem toMenuItem(final Element element)
	{
		final MenuItem menuItem = new MenuItem();
		Utils.parseMenuItem(menuItem, element.getAttribute("action"), element.getAttribute("match-scope"), element.getAttribute("match-mode")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// match target
		menuItem.matchTarget = element.getAttribute("match-target");

		// label
		final Element labelElement = DocumentAdapter.getFirstElementByTagName(element, "label"); //$NON-NLS-1$
		if (labelElement != null)
		{
			menuItem.label = labelElement.getTextContent();
		}

		// link
		final Element linkElement = DocumentAdapter.getFirstElementByTagName(element, "a"); //$NON-NLS-1$
		if (linkElement != null)
		{
			menuItem.link = linkElement.getAttribute("href"); //$NON-NLS-1$
			menuItem.target = linkElement.getAttribute("target"); //$NON-NLS-1$
		}

		return menuItem;
	}

	// H E L P E R S

	/**
	 * Find DOM element with given tag
	 *
	 * @param element starting DOM element
	 * @param tagName tag
	 * @return DOM element if found, null if none
	 */
	static private Element getFirstElementByTagName(final Element element, final String tagName)
	{
		if (element != null)
		{
			final NodeList elements = element.getElementsByTagName(tagName);
			if (elements.getLength() > 0)
			{
				return (Element) elements.item(0);
			}
		}
		return null;
	}

	/**
	 * Find DOM element with given tag among first level children
	 *
	 * @param element starting DOM element
	 * @param tagName tag
	 * @return DOM element if found, null if none
	 */
	static private Element getFirstLevel1ElementByTagName(final Element element, final String tagName)
	{
		final List<Element> childElements = DocumentAdapter.getLevel1ChildElementsByTagName(element, tagName);
		if (!childElements.isEmpty())
		{
			return childElements.get(0);
		}
		return null;
	}

	/**
	 * Find DOM elements with given tag among first level children
	 *
	 * @param element starting DOM element
	 * @param tagName tag
	 * @return DOM element if found, null if none
	 */
	static private List<Element> getLevel1ChildElementsByTagName(final Element element, final String tagName)
	{
		final ArrayList<Element> elements = new ArrayList<>();
		final NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++)
		{
			final Node node = children.item(i);
			if (node instanceof Element)
			{
				final Element childElement = (Element) node;
				if (childElement.getTagName().equals(tagName))
				{
					elements.add(childElement);
				}
			}
		}
		return elements;
	}
}
