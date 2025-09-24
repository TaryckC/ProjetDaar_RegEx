import "../css_sheets/Button.css";

type ButtonProps = {
    label: string;
    onClick?: () => void;
};

function Button({ label, onClick }: ButtonProps) {
    return <button onClick={onClick}>{label}</button>;
}

export default Button;