module com.metropolitan.cs103pznemanjavilic4050 {
    requires javafx.controls;
    requires javafx.fxml;
            
                        requires org.kordamp.bootstrapfx.core;
            
    opens com.metropolitan.cs103pznemanjavilic4050 to javafx.fxml;
    exports com.metropolitan.cs103pznemanjavilic4050;
}