import "../css_sheets/FilterPopUpInterface.css"

type FilterPopUpInterfaceProps = {
    onClose: () => void;
};

function FilterPopUpInterface({ onClose }: FilterPopUpInterfaceProps) {
    return (
        <div className="filter-popup-interface">
            <div className="language-setting">
                //TODO : Retrieve from server the list of all available languages and show them
            </div>
            <div className="author-setting">
                <input type="text" />
            </div>
            <button onClick={onClose}>Apply filters</button>
        </div>
    )
}

export default FilterPopUpInterface