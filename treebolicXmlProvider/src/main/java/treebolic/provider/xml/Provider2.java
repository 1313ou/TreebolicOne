package treebolic.provider.xml;

import java.net.URL;
import java.util.Properties;

import androidx.annotation.NonNull;
import treebolic.glue.iface.Image;
import treebolic.model.ImageDecorator;
import treebolic.model.Model;
import treebolic.model.MutableEdge;
import treebolic.model.MutableNode;

public class Provider2 extends treebolic.provider.xml.sax.Provider implements ImageDecorator
{
	// D E C O R A T I O N   M E M B E R S

	static String[] images = new String[]{root};

	static Image[] images2;

	// C O N S T R U C T O R

	/**
	 * Constructor
	 */
	public Provider2()
	{
		super();
		images2 = makeImages(images);
	}

	// P A R S E

	/*
	 * (non-Javadoc)
	 * @see treebolic.provider.IProvider#makeModel(java.lang.String, java.net.URL, java.util.Properties)
	 */
	@Override
	public Model makeModel(final String source, final URL base, final Properties parameters)
	{
		Model model = super.makeModel(source, base, parameters);
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
		if (index != -1)
		{
			edge.setImageIndex(index);
		}
	}

	// M A K E

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
