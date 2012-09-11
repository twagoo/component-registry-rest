package clarin.cmdi.componentregistry;

import clarin.cmdi.componentregistry.model.AbstractDescription;
import clarin.cmdi.componentregistry.rss.Category;
import clarin.cmdi.componentregistry.rss.Cloud;
import clarin.cmdi.componentregistry.rss.Image;
import clarin.cmdi.componentregistry.rss.Rss;
import clarin.cmdi.componentregistry.rss.RssChannel;

import clarin.cmdi.componentregistry.rss.RssItem;
import clarin.cmdi.componentregistry.rss.SkipDaysList;
import clarin.cmdi.componentregistry.rss.SkipHoursList;
import clarin.cmdi.componentregistry.rss.TextInput;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author olhsha
 */
public class RssCreator { // extends nothing so far, throuw nothing // make it abstract
    
    public List<AbstractDescription> descrs;
    
    protected String title;
    protected String link;
    protected String description;
    protected String language;
    protected String copyright;
    protected String managingEditor;
    protected String webMaster;
    protected String pubDate;
    protected String lastBuildDate;
    protected Category category;
    protected String generator;
    protected String docs;
    protected Cloud cloud;
    protected BigInteger ttl;
    protected Image image;
    protected String rating;
    protected TextInput textInput;
    protected SkipHoursList skipHours;
    protected SkipDaysList skipDays;
    
    protected BigDecimal version; // of an rss
    
    /**
     * 
     * @return 
     */
    public List<AbstractDescription> getDescriptions(){
        return descrs;
    }
    
    /**
     * 
     * @param descrs 
     */
    public void setDescriptions(List<AbstractDescription> descrs){
        this.descrs = descrs;
    }
    
    
    
    /**
     * 
     * @return 
     */
    
    
    public BigDecimal getVersion() {
        return version;
    }
    
    /**
     * 
     * @param version 
     */

    public void setVersion(BigDecimal version) {
        this.version = version;
    }
    
    /**
     * 
     * @return 
     */
    
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the link property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the value of the link property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLink(String value) {
        this.link = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     * Gets the value of the copyright property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCopyright(String value) {
        this.copyright = value;
    }

    /**
     * Gets the value of the managingEditor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManagingEditor() {
        return managingEditor;
    }

    /**
     * Sets the value of the managingEditor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManagingEditor(String value) {
        this.managingEditor = value;
    }

    /**
     * Gets the value of the webMaster property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWebMaster() {
        return webMaster;
    }

    /**
     * Sets the value of the webMaster property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWebMaster(String value) {
        this.webMaster = value;
    }

    /**
     * Gets the value of the pubDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPubDate() {
        return pubDate;
    }

    /**
     * Sets the value of the pubDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPubDate(String value) {
        this.pubDate = value;
    }

    /**
     * Gets the value of the lastBuildDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLastBuildDate() {
        return lastBuildDate;
    }

    /**
     * Sets the value of the lastBuildDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLastBuildDate(String value) {
        this.lastBuildDate = value;
    }

    /**
     * Gets the value of the category property.
     * 
     * @return
     *     possible object is
     *     {@link Category }
     *     
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets the value of the category property.
     * 
     * @param value
     *     allowed object is
     *     {@link Category }
     *     
     */
    public void setCategory(Category value) {
        this.category = value;
    }

    /**
     * Gets the value of the generator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * Sets the value of the generator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGenerator(String value) {
        this.generator = value;
    }

    /**
     * Gets the value of the docs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDocs() {
        return docs;
    }

    /**
     * Sets the value of the docs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDocs(String value) {
        this.docs = value;
    }

    /**
     * Gets the value of the cloud property.
     * 
     * @return
     *     possible object is
     *     {@link Cloud }
     *     
     */
    public Cloud getCloud() {
        return cloud;
    }

    /**
     * Sets the value of the cloud property.
     * 
     * @param value
     *     allowed object is
     *     {@link Cloud }
     *     
     */
    public void setCloud(Cloud value) {
        this.cloud = value;
    }

    /**
     * Gets the value of the ttl property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTtl() {
        return ttl;
    }

    /**
     * Sets the value of the ttl property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setTtl(BigInteger value) {
        this.ttl = value;
    }

    /**
     * Gets the value of the image property.
     * 
     * @return
     *     possible object is
     *     {@link Image }
     *     
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the value of the image property.
     * 
     * @param value
     *     allowed object is
     *     {@link Image }
     *     
     */
    public void setImage(Image value) {
        this.image = value;
    }

    /**
     * Gets the value of the rating property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRating() {
        return rating;
    }

    /**
     * Sets the value of the rating property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRating(String value) {
        this.rating = value;
    }

    /**
     * Gets the value of the textInput property.
     * 
     * @return
     *     possible object is
     *     {@link TextInput }
     *     
     */
    public TextInput getTextInput() {
        return textInput;
    }

    /**
     * Sets the value of the textInput property.
     * 
     * @param value
     *     allowed object is
     *     {@link TextInput }
     *     
     */
    public void setTextInput(TextInput value) {
        this.textInput = value;
    }

    /**
     * Gets the value of the skipHours property.
     * 
     * @return
     *     possible object is
     *     {@link SkipHoursList }
     *     
     */
    public SkipHoursList getSkipHours() {
        return skipHours;
    }

    /**
     * Sets the value of the skipHours property.
     * 
     * @param value
     *     allowed object is
     *     {@link SkipHoursList }
     *     
     */
    public void setSkipHours(SkipHoursList value) {
        this.skipHours = value;
    }

    /**
     * Gets the value of the skipDays property.
     * 
     * @return
     *     possible object is
     *     {@link SkipDaysList }
     *     
     */
    public SkipDaysList getSkipDays() {
        return skipDays;
    }

    /**
     * Sets the value of the skipDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link SkipDaysList }
     *     
     */
    public void setSkipDays(SkipDaysList value) {
        this.skipDays = value;
    }

    /**
     * Gets the value of the item property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the item property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getItem().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RssItem }
     * 
     * 
     */
    
    

    // creator method, this.desc to rssItem 
    private RssItem fromDescToRssItem(AbstractDescription desc) {



        RssItem retval = new RssItem();

        retval.setAuthor(desc.getCreatorName());
        // retval.setCategory(desc.???);
        // retval.setComments(desc.???);
        retval.setDescription(desc.getDescription());
        //retval.setEnclosure(desc.???);
        //retval.setGuid(desc.getId()); type mismatch
        retval.setLink(desc.getHref());
        retval.setPubDate(desc.getRegistrationDate());
        //retval.setSource(desc.???);
        retval.setTitle(desc.getName());


        return retval;

    }

    //makes (and returns ) an  rss out of a list of RssItems
    private Rss makeRssChannel(List<RssItem> rssItems) {

        final Rss rss = new Rss();
        final RssChannel channel = new RssChannel();
        
        channel.setCategory(category);
        channel.setCloud(cloud);
        channel.setCopyright(copyright);
        channel.setDescription(description);
        channel.setDocs(docs);
        channel.setGenerator(generator);
        channel.setImage(image);
        channel.setLanguage(language);
        channel.setLastBuildDate(lastBuildDate);
        channel.setLink(link);
        channel.setManagingEditor(managingEditor);
        channel.setPubDate(pubDate);
        channel.setRating(rating);
        channel.setSkipDays(skipDays);
        channel.setSkipHours(skipHours);
        channel.setTextInput(textInput);
        channel.setTitle(title);
        channel.setTtl(ttl);
        channel.setWebMaster(webMaster);
        
        rss.setChannel(channel);
        
        rss.setVersion(version);

        for (RssItem currentItem : rssItems) {
            channel.getItem().add(currentItem);
        }

        return rss;
    }

    //makes (nad returns) a list of items out a list of descriptions, return the pointer to the list of items
    private List<RssItem> makeListOfRssItems() {
        List<RssItem> listOfItems = new ArrayList<RssItem>();

        for (AbstractDescription currentDesc : descrs) {

            RssItem currentItem = fromDescToRssItem(currentDesc);
            listOfItems.add(currentItem);
        }

        return listOfItems;
    }

    //makes (and returns) a channel out a list of descriptions
    /**
     * 
     * @param descrs refers to the list of  component or profile descriptions, which is to be turn into an Rss
     * in principle, "descrs" parameter should not be null 
     * @return the reference to Rss created from descrs
     */
    public Rss makeRssChannel() {

        return (makeRssChannel(makeListOfRssItems()));
    }
}
