import {PropsWithChildren} from "react";
import {UseActionControlReturnType} from "@/hooks/useActionControl";

interface RowActionsProps {
  actionControl: UseActionControlReturnType
}

const ActionControlTableRowActions: React.FC<PropsWithChildren<RowActionsProps>> = ({actionControl,children}) => {

  return <div ref={actionControl.rowAction.ref} className={'row-actions'}>
    {children}
  </div>
};

export default ActionControlTableRowActions;