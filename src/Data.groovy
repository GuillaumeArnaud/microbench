import groovyx.gpars.actor.DefaultActor

class Data extends DefaultActor {

    private Collection<Collection<Object>> dataCollection
    private Iterator<Collection<Object>> iterator

    public Data(Collection<Collection<Object>> dataCollection) {
        this.dataCollection = dataCollection
        iterator = this.dataCollection.iterator()
    }

    protected Collection<Object> next() {
        if (dataCollection.size() == 0) return null
        else if (iterator.hasNext()) return iterator.next()
        else {
            // if the end of the collection has been reached, loop again
            iterator = dataCollection.iterator()
            return iterator.next()
        }
    }

    protected void reset() { iterator = dataCollection.iterator() }

}
