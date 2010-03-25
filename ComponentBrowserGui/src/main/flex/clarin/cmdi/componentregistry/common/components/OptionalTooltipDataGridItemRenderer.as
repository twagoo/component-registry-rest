package clarin.cmdi.componentregistry.common.components {
	import mx.controls.dataGridClasses.DataGridItemRenderer;

	public class OptionalTooltipDataGridItemRenderer extends DataGridItemRenderer {
		public function OptionalTooltipDataGridItemRenderer() {
			super();
		}

        public override function set toolTip(value:String):void {
            if (textWidth > width) {
                super.toolTip = value;
            }
        }
	}
}