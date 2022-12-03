package treebolic.provider.owl;

import java.util.Properties;

import androidx.annotation.NonNull;
import treebolic.glue.iface.Image;
import treebolic.model.ImageDecorator;
import treebolic.model.Model;
import treebolic.model.MutableEdge;
import treebolic.model.MutableNode;
import treebolic.provider.owl.jena.BaseProvider;
import treebolic.provider.owl.jena.OwlModelFactory;

public class Provider2 extends BaseProvider
{
	protected OwlModelFactory factory(@NonNull Properties properties)
	{
		return new AndroidOwlModelFactory(properties);
	}

	private static class AndroidOwlModelFactory extends OwlModelFactory implements ImageDecorator
	{
		// D E C O R A T I O N   M E M B E R S

		static Image[] images2;

		// C O N S T R U C T O R

		/**
		 * Constructor
		 *
		 * @param properties properties
		 */
		public AndroidOwlModelFactory(final Properties properties)
		{
			super(properties);
			images2 = makeImages(images);
			loadBalancer.setGroupNode(null, LOADBALANCING_BACKCOLOR, LOADBALANCING_FORECOLOR, LOADBALANCING_EDGECOLOR, LOADBALANCING_EDGE_STYLE, LOADBALANCING_IMAGEINDEX, LOADBALANCING_IMAGE, null);
			instancesLoadBalancer.setGroupNode(null, LOADBALANCING_INSTANCES_BACKCOLOR, LOADBALANCING_INSTANCES_FORECOLOR, LOADBALANCING_INSTANCES_EDGECOLOR, LOADBALANCING_INSTANCES_EDGE_STYLE, LOADBALANCING_INSTANCES_IMAGEINDEX, LOADBALANCING_INSTANCES_IMAGE, null);
			propertiesLoadBalancer.setGroupNode(null, LOADBALANCING_PROPERTIES_BACKCOLOR, LOADBALANCING_PROPERTIES_FORECOLOR, LOADBALANCING_PROPERTIES_EDGECOLOR, LOADBALANCING_PROPERTIES_EDGE_STYLE, LOADBALANCING_PROPERTIES_IMAGEINDEX, LOADBALANCING_PROPERTIES_IMAGE, null);
		}

		// P A R S E

		/**
		 * Make model
		 *
		 * @param ontologyUrlString ontology URL string
		 * @return model if successful
		 */
		@Override
		public Model makeModel(final String ontologyUrlString)
		{
			Model model = super.makeModel(ontologyUrlString);
			return new Model(model.tree, model.settings, images2);
		}

		// D E C O R A T E

		@Override
		public void setNodeImage(final MutableNode node, final int index)
		{
			if (index != -1)
			{
				node.setImageIndex(index);
			}
		}

		@Override
		public void setTreeEdgeImage(final MutableNode node, final int index)
		{
			if (index != -1)
			{
				node.setEdgeImageIndex(index);
			}
		}

		@Override
		public void setEdgeImage(final MutableEdge edge, final int index)
		{

		}

		/**
		 * ImageFactory
		 *
		 * @param imageUrls image urls
		 * @return images
		 */
		public Image[] makeImages(@NonNull final String[] imageUrls)
		{
			Image[] images = new Image[imageUrls.length];
			for (int i = 0; i < imageUrls.length; i++)
			{
				images[i] = new treebolic.glue.Image(Provider2.class.getResource("images/" + imageUrls[i]));
			}
			return images;
		}
	}
}
