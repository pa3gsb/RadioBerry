(function() {

(function(pkg, Class) {

var Panel = zebra.ui.Panel,
    Label = zebra.ui.Label,
    Border = zebra.ui.Border,
    BorderPan = zebra.ui.BorderPan;

pkg.createLabel = function (txt, color, font) {
    color = color || zebra.ui.palette.gray1;
    var l = new Label(txt.indexOf("\n") >= 0 ? new zebra.data.Text(txt) : txt);
    l.setColor(color);
    if (font) l.setFont(font);  
    else l.setFont(zebra.ui.boldFont);
    l.setBorder(new Border(zebra.util.rgb.gray));
    l.setPadding(4);
    return l;
};

pkg.createBorderPan = function (txt, content, w, h) {
    content = content || new Panel();
    var bp = new BorderPan(txt, content);
    content.setPadding(4);
    w = w || -1;
    h = h || -1;
    bp.setPreferredSize(w, h);
    return bp;
};

pkg.DemoPan = Class(Panel, [
    function() {
        this.$super();
        this.setPadding(6);
    },

    function activated(b) {}
]);

zebra.ui.configure(function(conf) {
    conf.loadByUrl(pkg.$url + "zebra-radio.json");
});

})(zebra("ui.radio"), zebra.Class);


(function(pkg, Class) {

eval(zebra.Import("ui", "layout"))

pkg.LayoutDemo = new Class(pkg.DemoPan, [
    function() {
        this.$super();
        this.setLayout(new BorderLayout());
        //var n = new Tabs(BOTTOM);
        //n.add("Border layout", this.borderLayoutPage());
		//n.add("Flow layout", this.flowLayoutPage());
        //this.add(CENTER, n);
        
        var titel = new BoldLabel("Software Defined Radio");
        this.add(TOP, titel);
        
    },

    function borderLayoutPage() {
        var bl_p = new Panel(new BorderLayout(2,2));
        bl_p.setPadding(4);
        bl_p.add(TOP, new Button("TOP"));
        bl_p.add(BOTTOM, new Button("BOTTOM"));
        bl_p.add(RIGHT, new Button("RIGHT"));
        bl_p.add(LEFT, new Button("LEFT"));
        bl_p.add(CENTER, new Button("CENTER"));
        return bl_p;
    },
	
	    function flowLayoutPage() {
			var fl = new Panel(new ListLayout(4));
			fl.setPadding(4);
			var fl_1 = new Panel(new FlowLayout(LEFT, CENTER, HORIZONTAL, 4));
			var fl_2 = new Panel(new FlowLayout(CENTER, CENTER, HORIZONTAL, 4));
			var fl_3 = new Panel(new FlowLayout(RIGHT, CENTER, HORIZONTAL, 4));
			var fl_4 = new Panel(new FlowLayout(CENTER, CENTER, VERTICAL, 4));
			var fl_5 = new Panel(new FlowLayout(RIGHT, BOTTOM, VERTICAL, 4));
			fl.add(pkg.createBorderPan("Left aligned, horizontal", fl_1));
			fl.add(pkg.createBorderPan("Centered aligned, horizontal", fl_2));
			fl.add(pkg.createBorderPan("Right aligned, horizontal", fl_3));
			fl_1.add(pkg.createLabel("Component 1"));
			fl_2.add(pkg.createLabel("Component 1"));
			fl_3.add(pkg.createLabel("Component 1"));
			fl_4.add(pkg.createLabel("Component 1"));
			fl_5.add(pkg.createLabel("Component 1"));
			fl_1.add(pkg.createLabel("Component 2"));
			fl_2.add(pkg.createLabel("Component 2"));
			fl_3.add(pkg.createLabel("Component 2"));
			fl_4.add(pkg.createLabel("Component 2"));
			fl_5.add(pkg.createLabel("Component 2"));
			fl_1.add(pkg.createLabel("Component 3"));
			fl_2.add(pkg.createLabel("Component 3"));
			fl_3.add(pkg.createLabel("Component 3"));
			fl_4.add(pkg.createLabel("Component 3"));
			fl_5.add(pkg.createLabel("Component 3"));

			var p2 = new Panel(new PercentLayout());
			var ps = fl_5.getPreferredSize();
			fl_4.setPreferredSize(-1, ps.height + 40);
			fl_5.setPreferredSize(-1, ps.height + 40);

			p2.add(50, pkg.createBorderPan("Centered aligned, vertical", fl_4));
			p2.add(50, pkg.createBorderPan("Right-bottom aligned, vertical", fl_5));

			fl.add(p2);
        return fl;
    }
]);
})(zebra.ui.radio, zebra.Class);
})();