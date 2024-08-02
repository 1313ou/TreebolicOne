/*
 * Copyright (c) 2023. Bernard Bou
 */
package treebolic.provider.owl

import treebolic.glue.iface.Image
import treebolic.model.ImageDecorator
import treebolic.model.Model
import treebolic.model.MutableEdge
import treebolic.model.MutableNode
import treebolic.provider.owl.sax.BaseProvider
import treebolic.provider.owl.sax.OwlModelFactory
import java.util.Properties

/**
 * Provider (with image decoration)
 */
class Provider2 : BaseProvider() {

    override fun factory(properties: Properties): OwlModelFactory {
        return AndroidOwlModelFactory(properties)
    }

    /**
     * Model factory
     *
     * @param properties properties
     */
    private class AndroidOwlModelFactory(properties: Properties?) : OwlModelFactory(properties), ImageDecorator {

        init {
            images2 = makeImages(images)
            loadBalancer.setGroupNode(null, LOADBALANCING_BACKCOLOR, LOADBALANCING_FORECOLOR, LOADBALANCING_EDGECOLOR, LOADBALANCING_EDGE_STYLE, LOADBALANCING_IMAGEINDEX, LOADBALANCING_IMAGE, null)
            instancesLoadBalancer.setGroupNode(
                null,
                LOADBALANCING_INSTANCES_BACKCOLOR,
                LOADBALANCING_INSTANCES_FORECOLOR,
                LOADBALANCING_INSTANCES_EDGECOLOR,
                LOADBALANCING_INSTANCES_EDGE_STYLE,
                LOADBALANCING_INSTANCES_IMAGEINDEX,
                LOADBALANCING_INSTANCES_IMAGE,
                null
            )
            propertiesLoadBalancer.setGroupNode(
                null,
                LOADBALANCING_PROPERTIES_BACKCOLOR,
                LOADBALANCING_PROPERTIES_FORECOLOR,
                LOADBALANCING_PROPERTIES_EDGECOLOR,
                LOADBALANCING_PROPERTIES_EDGE_STYLE,
                LOADBALANCING_PROPERTIES_IMAGEINDEX,
                LOADBALANCING_PROPERTIES_IMAGE,
                null
            )
        }

        // P A R S E

        /**
         * Make model
         *
         * @param ontologyUrlString ontology URL string
         * @return model if successful
         */
        override fun makeModel(ontologyUrlString: String): Model {
            val model = super.makeModel(ontologyUrlString)
            model.settings.fontSize = 12
            return Model(model.tree, model.settings, images2)
        }

        // D E C O R A T E

        override fun setNodeImage(node: MutableNode, index: Int) {
            if (index != -1) {
                node.imageIndex = index
            }
        }

        override fun setTreeEdgeImage(node: MutableNode, index: Int) {
            if (index != -1) {
                node.edgeImageIndex = index
            }
        }

        override fun setEdgeImage(edge: MutableEdge, index: Int) {
            if (index != -1) {
                edge.imageIndex = index
            }
        }

        /**
         * ImageFactory
         *
         * @param imageUrls image urls
         * @return images
         */
        fun makeImages(imageUrls: Array<String>): Array<Image?> {
            val images = arrayOfNulls<Image>(imageUrls.size)
            for (i in imageUrls.indices) {
                images[i] = treebolic.glue.Image(Provider2::class.java.getResource("images/" + imageUrls[i]))
            }
            return images
        }

        companion object {

            lateinit var images2: Array<Image?>
        }
    }
}
