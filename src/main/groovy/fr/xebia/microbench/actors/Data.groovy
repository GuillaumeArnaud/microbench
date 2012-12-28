package fr.xebia.microbench.actors

class Data {

    private Collection<Object>[] dataCollection
    private Iterator<Collection<Object>> iterator

    public Data(Collection<Object>... dataCollection) {
        this.dataCollection = dataCollection
        iterator = this.dataCollection.iterator()
    }

    public Collection<Object> next() {
        if (dataCollection.length == 0) return null
        else if (iterator.hasNext()) return iterator.next()
        else {
            // if the end of the collection has been reached, loop again
            iterator = dataCollection.iterator()
            return iterator.next()
        }
    }

    public void reset() { iterator = dataCollection.iterator() }

}
