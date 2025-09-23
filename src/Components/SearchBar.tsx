import "../css_sheets/SearchBar.css"
import searchButtonIcon from "../assets/search-button.svg"
import settingsButtonIcon from "../assets/6559635-200.png"

//TODO : Usage of use state to take care of the query when search button is clicked.

type SearchBarProps = {
    type: number; // 1 : Main search bar (Used in home page) | Any other number : Secondary search Bar (Used after a research)
    onSearch?: (query: string) => void; // Called after "Enter" has been pressed.
    placeholder?: string;
    initialQuery?: string;
}

function SearchBar({ type, onSearch, placeholder, initialQuery }: SearchBarProps) {
    if (type === 1) {
        return (
            <div className="main-search">
                <div className="main-search-inputs">
                    <div className="main-data-results">
                        <input type="text" placeholder={placeholder} />
                        <div className="main-search-options">
                            <div className="main-search-filter-button">
                                <img src={settingsButtonIcon} />
                            </div>
                            <div className="main-search-button">
                                <img src={searchButtonIcon} />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
    else {

    }
}

export default SearchBar;