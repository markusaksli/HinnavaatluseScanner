class Offer {
    String name;
    String price;
    String aval;

    Offer(String n, String p, String a){
        name = n;
        price = p;
        aval = a;
    }
    boolean equals(Offer o){
        return this.name.equals(o.name) && this.price.equals(o.price) && this.aval.equals(o.aval);
    }
}
