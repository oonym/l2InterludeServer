/**
 * 
 */
package net.sf.l2j.gameserver.model;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import net.sf.l2j.Config;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Multisell list manager
 *
 */
public class L2Multisell
{
    private static Logger _log = Logger.getLogger(L2Multisell.class.getName());
    private List<MultiSellListContainer> entries = new FastList<MultiSellListContainer>();
    private static L2Multisell _instance = new L2Multisell();

    public MultiSellListContainer getList(int id)
    {
        synchronized (entries)
        {
            for (MultiSellListContainer list : entries)
            {
                if (list.getListId() == id) return list;
            }
        }

        _log.warning("[L2Multisell] cant find list with id: " + id);
        return null;
    }

    public L2Multisell()
    {
        parseData();
    }

    public void reload()
    {
        parseData();
    }

    public static L2Multisell getInstance()
    {
        return _instance;
    }

    private void parseData()
    {
        entries.clear();
        parse();
    }

    public class MultiSellEntry
    {
        private int _entryId;
        private int _productId;
        private int _productCount;
        private int _productEnchant;
        private List<MultiSellIngredient> _ingredients = new FastList<MultiSellIngredient>();

        /**
         * @param entryId The entryId to set.
         */
        public void setEntryId(int entryId)
        {
            _entryId = entryId;
        }

        /**
         * @return Returns the entryId.
         */
        public int getEntryId()
        {
            return _entryId;
        }

        /**
         * @param productId The productId to set.
         */
        public void setProductId(int productId)
        {
            _productId = productId;
        }

        /**
         * @return Returns the productId.
         */
        public int getProductId()
        {
            return _productId;
        }

        /**
         * @param productCount The productCount to set.
         */
        public void setProductCount(int productCount)
        {
            _productCount = productCount;
        }

        /**
         * @return Returns the productCount.
         */
        public int getProductCount()
        {
            return _productCount;
        }

        /**
         * @param productEnchant The productEnchant to set.
         */
        public void setProductEnchant(int productEnchant)
        {
            _productEnchant = productEnchant;
        }

        /**
         * @return Returns the productEnchant.
         */
        public int getProductEnchant()
        {
            return _productEnchant;
        }

        /**
         * @param ingredients The ingredients to set.
         */
        public void addIngredient(MultiSellIngredient ingredient)
        {
            _ingredients.add(ingredient);
        }

        /**
         * @return Returns the ingredients.
         */
        public List<MultiSellIngredient> getIngredients()
        {
            return _ingredients;
        }
    }

    public class MultiSellIngredient
    {
        private int _itemId;
        private int _itemCount;
        private int _itemEnchant;

        public MultiSellIngredient(int itemId, int itemCount, int itemEnchant)
        {
            setItemId(itemId);
            setItemCount(itemCount);
            setItemEnchant(itemEnchant);
        }

        /**
         * @param itemId The itemId to set.
         */
        public void setItemId(int itemId)
        {
            _itemId = itemId;
        }

        /**
         * @return Returns the itemId.
         */
        public int getItemId()
        {
            return _itemId;
        }

        /**
         * @param itemCount The itemCount to set.
         */
        public void setItemCount(int itemCount)
        {
            _itemCount = itemCount;
        }

        /**
         * @return Returns the itemCount.
         */
        public int getItemCount()
        {
            return _itemCount;
        }

        /**
         * @param itemEnchant The itemEnchant to set.
         */
        public void setItemEnchant(int itemEnchant)
        {
            _itemEnchant = itemEnchant;
        }

        /**
         * @return Returns the itemEnchant.
         */
        public int getItemEnchant()
        {
            return _itemEnchant;
        }
    }

    public class MultiSellListContainer
    {
        private int _listId;
        List<MultiSellEntry> entriesC;
        
        public MultiSellListContainer()
        {
            entriesC = new FastList<MultiSellEntry>();
        }

        /**
         * @param listId The listId to set.
         */
        public void setListId(int listId)
        {
            _listId = listId;
        }

        /**
         * @return Returns the listId.
         */
        public int getListId()
        {
            return _listId;
        }

        public void addEntry(MultiSellEntry e)
        {
            entriesC.add(e);
        }

        public List<MultiSellEntry> getEntries()
        {
            return entriesC;
        }
    }

    private void hashFiles(String dirname, List<File> hash)
    {
        File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
        if (!dir.exists())
        {
            _log.config("Dir " + dir.getAbsolutePath() + " not exists");
            return;
        }
        File[] files = dir.listFiles();
        for (File f : files)
        {
            if (f.getName().endsWith(".xml")) hash.add(f);
        }
    }

    private void parse()
    {
        Document doc = null;
        int id = 0;
        List<File> files = new FastList<File>();
        hashFiles("multisell", files);

        for (File f : files)
        {
            id = Integer.parseInt(f.getName().replaceAll(".xml", ""));
            try
            {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                doc = factory.newDocumentBuilder().parse(f);
            }
            catch (Exception e)
            {
                _log.log(Level.SEVERE, "Error loading file " + f, e);
            }
            try
            {
                MultiSellListContainer list = parseDocument(doc);
                list.setListId(id);
                entries.add(list);
            }
            catch (Exception e)
            {
                _log.log(Level.SEVERE, "Error in file " + f, e);
            }
        }
    }

    protected MultiSellListContainer parseDocument(Document doc)
    {
        MultiSellListContainer list = new MultiSellListContainer();

        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if ("list".equalsIgnoreCase(n.getNodeName()))
            {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if ("item".equalsIgnoreCase(d.getNodeName()))
                    {
                        MultiSellEntry e = parseEntry(d);
                        list.addEntry(e);
                    }
                }
            }
            else if ("item".equalsIgnoreCase(n.getNodeName()))
            {
                MultiSellEntry e = parseEntry(n);
                list.addEntry(e);
            }
        }

        return list;
    }

    protected MultiSellEntry parseEntry(Node n)
    {
        int entryId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());

        Node first = n.getFirstChild();
        MultiSellEntry entry = new MultiSellEntry();

        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("ingredient".equalsIgnoreCase(n.getNodeName()))
            {
                int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
                int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
                int enchant = Integer.parseInt(n.getAttributes().getNamedItem("enchant").getNodeValue());

                MultiSellIngredient e = new MultiSellIngredient(id, count, enchant);
                entry.addIngredient(e);
            }
            else if ("production".equalsIgnoreCase(n.getNodeName()))
            {
                int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
                int count = Integer.parseInt(n.getAttributes().getNamedItem("count").getNodeValue());
                int enchant = Integer.parseInt(n.getAttributes().getNamedItem("enchant").getNodeValue());

                entry.setEntryId(entryId);
                entry.setProductId(id);
                entry.setProductCount(count);
                entry.setProductEnchant(enchant);
            }
        }

        return entry;
    }

}
