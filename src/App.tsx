import logo from './assets/book.6fc700a9.png';
import './App.css'
import SearchBar from './Components/SearchBar';

function App() {
  return (
    <>
      <div id="AppCentralView">
        <a href="https://www.gutenberg.org" target='_blank' rel="noopener noreferrer">
          <img id="logo" src={logo} />
        </a>
        <SearchBar type={1} onSearch={(query) => { console.log("Search bar has been used.") }} placeholder='Search for a book ?' />
      </div>
    </>
  )
}

export default App
